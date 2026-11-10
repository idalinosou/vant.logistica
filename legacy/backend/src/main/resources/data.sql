
INSERT INTO clientes(nome,documento,telefone,email,cidade)
SELECT 'Cliente Demonstração','00.000.000/0001-00','(31) 3000-0000','contato@exemplo.com','Betim - MG'
WHERE NOT EXISTS (SELECT 1 FROM clientes);
INSERT INTO motoristas(nome,documento,telefone,cnh,status)
SELECT 'João Silva','000.000.000-00','(31) 99999-0000','12345678900','Ativo'
WHERE NOT EXISTS (SELECT 1 FROM motoristas);
INSERT INTO veiculos(placa,modelo,tipo,capacidade,status)
SELECT 'ABC1D23','Caminhão Baú 01','Caminhão','12 toneladas','Disponível'
WHERE NOT EXISTS (SELECT 1 FROM veiculos);
INSERT INTO cargas(codigo,origem,destino,cliente,motorista,veiculo,status,previsao_entrega)
SELECT 'CAR-001','Betim - MG','Belo Horizonte - MG','Cliente Demonstração','João Silva','Caminhão Baú 01','Em trânsito',CURRENT_DATE + 1
WHERE NOT EXISTS (SELECT 1 FROM cargas);
INSERT INTO cargas(codigo,origem,destino,cliente,motorista,veiculo,status,previsao_entrega)
SELECT 'CAR-002','Contagem - MG','Sete Lagoas - MG','Cliente Demonstração','João Silva','Caminhão Baú 01','Pendente',CURRENT_DATE + 2
WHERE NOT EXISTS (SELECT 1 FROM cargas WHERE codigo='CAR-002');
INSERT INTO cargas(codigo,origem,destino,cliente,motorista,veiculo,status,previsao_entrega)
SELECT 'CAR-003','Belo Horizonte - MG','Betim - MG','Cliente Demonstração','João Silva','Caminhão Baú 01','Entregue',CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM cargas WHERE codigo='CAR-003');
