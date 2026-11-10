package com.vant.api;

import java.sql.Date;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class ApiController {
 private final JdbcTemplate db;
 public ApiController(JdbcTemplate db) { this.db = db; }

 @GetMapping("/status")
 public Map<String,Object> status() { return Map.of("sistema","VANT LOGÍSTICA","mensagem","API funcionando corretamente!","status","ONLINE"); }

 @GetMapping("/dashboard")
 public Map<String,Object> dashboard() {
  return Map.of(
   "totalCargas", count("cargas"),
   "pendentes", scalar("SELECT COUNT(*) FROM cargas WHERE lower(status)='pendente'"),
   "emTransito", scalar("SELECT COUNT(*) FROM cargas WHERE lower(status) IN ('em trânsito','em transito')"),
   "entregues", scalar("SELECT COUNT(*) FROM cargas WHERE lower(status)='entregue'"),
   "veiculos", count("veiculos"), "motoristas", count("motoristas"), "clientes", count("clientes"));
 }
 private int count(String table) { return scalar("SELECT COUNT(*) FROM " + table); }
 private int scalar(String sql) { Integer n=db.queryForObject(sql,Integer.class); return n==null?0:n; }

 @GetMapping("/cargas")
 public List<Map<String,Object>> cargas() { return db.queryForList("SELECT * FROM cargas ORDER BY id DESC"); }

 @PostMapping("/cargas")
 public Map<String,Object> criarCarga(@RequestBody Map<String,Object> p) {
  String codigo=req(p,"codigo"), origem=req(p,"origem"), destino=req(p,"destino");
  String status=val(p,"status","Pendente");
  db.update("INSERT INTO cargas(codigo,origem,destino,cliente,motorista,veiculo,status,previsao_entrega) VALUES (?,?,?,?,?,?,?,?)",
   codigo,origem,destino,val(p,"cliente",""),val(p,"motorista",""),val(p,"veiculo",""),status,date(p.get("previsao_entrega")));
  return db.queryForMap("SELECT * FROM cargas WHERE codigo=?",codigo);
 }
 @PutMapping("/cargas/{id}")
 public Map<String,Object> atualizarCarga(@PathVariable long id,@RequestBody Map<String,Object> p) {
  Map<String,Object> old=db.queryForMap("SELECT * FROM cargas WHERE id=?",id);
  String novo=req(p,"status"); String anterior=String.valueOf(old.get("status"));
  db.update("UPDATE cargas SET codigo=?,origem=?,destino=?,cliente=?,motorista=?,veiculo=?,status=?,previsao_entrega=? WHERE id=?",
   req(p,"codigo"),req(p,"origem"),req(p,"destino"),val(p,"cliente",""),val(p,"motorista",""),val(p,"veiculo",""),novo,date(p.get("previsao_entrega")),id);
  if(!Objects.equals(anterior,novo)) db.update("INSERT INTO historico_carga(carga_id,status_anterior,novo_status) VALUES(?,?,?)",id,anterior,novo);
  return db.queryForMap("SELECT * FROM cargas WHERE id=?",id);
 }
 @DeleteMapping("/cargas/{id}") public ResponseEntity<Void> excluirCarga(@PathVariable long id) { db.update("DELETE FROM cargas WHERE id=?",id); return ResponseEntity.noContent().build(); }
 @GetMapping("/cargas/{id}/historico") public List<Map<String,Object>> historico(@PathVariable long id) {
  return db.queryForList("SELECT id,carga_id,status_anterior AS \"statusAnterior\",novo_status AS \"novoStatus\",data_alteracao AS \"dataAlteracao\" FROM historico_carga WHERE carga_id=? ORDER BY data_alteracao DESC",id);
 }

 @GetMapping("/veiculos") public List<Map<String,Object>> veiculos(){return db.queryForList("SELECT * FROM veiculos ORDER BY id DESC");}
 @PostMapping("/veiculos") public Map<String,Object> criarVeiculo(@RequestBody Map<String,Object> p){
  db.update("INSERT INTO veiculos(placa,modelo,tipo,capacidade,status) VALUES(?,?,?,?,?)",req(p,"placa").toUpperCase(),req(p,"modelo"),val(p,"tipo","Caminhão"),val(p,"capacidade",""),val(p,"status","Disponível"));
  return db.queryForMap("SELECT * FROM veiculos WHERE placa=?",req(p,"placa").toUpperCase());
 }
 @DeleteMapping("/veiculos/{id}") public ResponseEntity<Void> excluirVeiculo(@PathVariable long id){db.update("DELETE FROM veiculos WHERE id=?",id);return ResponseEntity.noContent().build();}

 @GetMapping("/motoristas") public List<Map<String,Object>> motoristas(){return db.queryForList("SELECT * FROM motoristas ORDER BY id DESC");}
 @PostMapping("/motoristas") public Map<String,Object> criarMotorista(@RequestBody Map<String,Object> p){
  db.update("INSERT INTO motoristas(nome,documento,telefone,cnh,status) VALUES(?,?,?,?,?)",req(p,"nome"),val(p,"documento",""),val(p,"telefone",""),val(p,"cnh",""),val(p,"status","Ativo"));
  return db.queryForMap("SELECT * FROM motoristas ORDER BY id DESC LIMIT 1");
 }
 @DeleteMapping("/motoristas/{id}") public ResponseEntity<Void> excluirMotorista(@PathVariable long id){db.update("DELETE FROM motoristas WHERE id=?",id);return ResponseEntity.noContent().build();}

 @GetMapping("/clientes") public List<Map<String,Object>> clientes(){return db.queryForList("SELECT * FROM clientes ORDER BY id DESC");}
 @PostMapping("/clientes") public Map<String,Object> criarCliente(@RequestBody Map<String,Object> p){
  db.update("INSERT INTO clientes(nome,documento,telefone,email,cidade) VALUES(?,?,?,?,?)",req(p,"nome"),val(p,"documento",""),val(p,"telefone",""),val(p,"email",""),val(p,"cidade",""));
  return db.queryForMap("SELECT * FROM clientes ORDER BY id DESC LIMIT 1");
 }
 @DeleteMapping("/clientes/{id}") public ResponseEntity<Void> excluirCliente(@PathVariable long id){db.update("DELETE FROM clientes WHERE id=?",id);return ResponseEntity.noContent().build();}

 private String req(Map<String,Object> p,String k){String s=val(p,k,"").trim();if(s.isEmpty())throw new IllegalArgumentException("O campo "+k+" é obrigatório.");return s;}
 private String val(Map<String,Object> p,String k,String d){Object v=p.get(k);return v==null?d:String.valueOf(v);}
 private Date date(Object v){if(v==null||String.valueOf(v).isBlank())return null;return Date.valueOf(String.valueOf(v));}

 @ExceptionHandler(Exception.class)
 public ResponseEntity<Map<String,String>> erro(Exception e) {
  String msg=e.getMessage()==null?"Erro interno":e.getMessage();
  return ResponseEntity.badRequest().body(Map.of("erro",msg));
 }
}
