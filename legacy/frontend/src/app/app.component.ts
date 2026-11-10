import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

type Modulo='Dashboard'|'Cargas'|'Frota'|'Motoristas'|'Clientes';
@Component({
 selector:'app-root', standalone:true, imports:[CommonModule,FormsModule],
 templateUrl:'./app.component.html'
})
export class AppComponent implements OnInit {
 api='http://localhost:8080/api';
 modulo:Modulo='Dashboard';
 dashboard:any={totalCargas:0,pendentes:0,emTransito:0,entregues:0,veiculos:0,motoristas:0,clientes:0};
 cargas:any[]=[]; veiculos:any[]=[]; motoristas:any[]=[]; clientes:any[]=[];
 historico:any[]=[]; cargaHistorico:any=null; erro=''; sucesso='';
 form:any={};
 constructor(private http:HttpClient){}
 ngOnInit(){this.atualizarTudo();}
 abrir(m:Modulo){this.modulo=m;this.form={};this.erro='';this.sucesso='';}
 atualizarTudo(){this.http.get<any>(`${this.api}/dashboard`).subscribe({next:r=>this.dashboard=r,error:()=>this.erro='Não foi possível conectar à API.'});
  this.http.get<any[]>(`${this.api}/cargas`).subscribe(r=>this.cargas=r);
  this.http.get<any[]>(`${this.api}/veiculos`).subscribe(r=>this.veiculos=r);
  this.http.get<any[]>(`${this.api}/motoristas`).subscribe(r=>this.motoristas=r);
  this.http.get<any[]>(`${this.api}/clientes`).subscribe(r=>this.clientes=r);}
 salvar(){
  this.erro='';this.sucesso='';
  const endpoint=this.modulo==='Cargas'?'cargas':this.modulo==='Frota'?'veiculos':this.modulo==='Motoristas'?'motoristas':'clientes';
  const payload=this.modulo==='Cargas'?this.form:this.form;
  this.http.post(`${this.api}/${endpoint}`,payload).subscribe({next:()=>{
   this.sucesso='Cadastro realizado com sucesso!';this.form={};this.atualizarTudo();
  },error:e=>this.erro=e.error?.erro||'Não foi possível salvar. Verifique os campos e a conexão.'});
 }
 excluir(tipo:string,id:number){if(!confirm('Confirma a exclusão deste registro?'))return;
  this.http.delete(`${this.api}/${tipo}/${id}`).subscribe({next:()=>{this.sucesso='Registro excluído.';this.atualizarTudo();},error:e=>this.erro=e.error?.erro||'Não foi possível excluir.'});}
 abrirHistorico(c:any){this.cargaHistorico=c;this.http.get<any[]>(`${this.api}/cargas/${c.id}/historico`).subscribe(r=>this.historico=r);}
 editarCarga(c:any){this.form={...c};this.erro='';this.sucesso='';}
 atualizarCarga(){const id=this.form.id;this.http.put(`${this.api}/cargas/${id}`,this.form).subscribe({next:()=>{this.sucesso='Carga atualizada!';this.form={};this.atualizarTudo();},error:e=>this.erro=e.error?.erro||'Erro ao atualizar.'});}
 cancelarEdicao(){this.form={};}
}
