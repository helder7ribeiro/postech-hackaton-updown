# language: pt
Funcionalidade: Gerenciamento de Jobs
  Como um usuário da aplicação
  Eu quero poder criar, consultar e deletar meus jobs de processamento de vídeo
  Para gerenciar minhas tarefas

  Cenário: Criar um novo job com sucesso
    Dado que já existe um usuário com e-mail "usuario.job@teste.com"
    Quando o usuário "usuario.job@teste.com" envia um vídeo para criar um job
    Entao o status da resposta deve ser 201
    E o corpo da resposta deve conter o e-mail do usuário "usuario.job@teste.com"
    E um job para o usuário "usuario.job@teste.com" deve existir no banco de dados
    E o vídeo do job deve existir no S3

  Cenário: Tentar criar um job para um usuário inexistente
    Quando um usuário inexistente envia um vídeo para criar um job
    Entao o status da resposta deve ser 404

  Cenário: Buscar um job por ID com sucesso
    Dado que o usuário "usuario.job.busca@teste.com" criou um job
    Quando um usuário envia uma requisição GET para a URL do último job criado
    Entao o status da resposta deve ser 200
    E o corpo da resposta deve conter o e-mail do usuário "usuario.job.busca@teste.com"

  Cenário: Deletar um job com sucesso
    Dado que o usuário "usuario.job.delete@teste.com" criou um job
    Quando um usuário envia uma requisição DELETE para a URL do último job criado
    Entao o status da resposta deve ser 204
    E o último job criado não deve mais existir no banco de dados
