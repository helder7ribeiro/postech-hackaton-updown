# language: pt
Funcionalidade: Gerenciamento de Usuários
  Como um cliente da API, eu quero criar, ler, atualizar e deletar usuários
  para gerenciar as contas na plataforma.

  Contexto:
    Dado que o sistema está limpo

  Cenário: Criar um novo usuário com sucesso
    Quando um usuário envia uma requisição POST para "/api/v1/app-users" com o corpo:
    """
    {
      "email": "novo.usuario@example.com",
      "username": "novo.usuario"
    }
    """
    Então o status da resposta deve ser 201
    E o corpo da resposta deve conter o e-mail "novo.usuario@example.com"
    E um usuário com e-mail "novo.usuario@example.com" deve existir no banco de dados

  Cenário: Tentar criar um usuário com e-mail já existente
    Dado que já existe um usuário com e-mail "existente@example.com"
    Quando um usuário envia uma requisição POST para "/api/v1/app-users" com o corpo:
    """
    {
      "email": "existente@example.com",
      "username": "existente"
    }
    """
    Então o status da resposta deve ser 409

  Cenário: Buscar um usuário por ID com sucesso
    Dado que já existe um usuário com e-mail "buscar.id@example.com"
    Quando um usuário envia uma requisição GET para a URL do usuário com e-mail "buscar.id@example.com"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve conter o e-mail "buscar.id@example.com"

  Cenário: Tentar buscar um usuário com ID inexistente
    Quando um usuário envia uma requisição GET para "/api/v1/app-users/11111111-1111-1111-1111-111111111111"
    Então o status da resposta deve ser 404

  Cenário: Atualizar o e-mail de um usuário com sucesso
    Dado que já existe um usuário com e-mail "antigo.email@example.com"
    Quando um usuário envia uma requisição PUT para a URL do usuário com e-mail "antigo.email@example.com" com o corpo:
    """
    {
      "email": "novo.email@example.com",
      "username": "novo.email"
    }
    """
    Então o status da resposta deve ser 200
    E o corpo da resposta deve conter o e-mail "novo.email@example.com"

  Cenário: Deletar um usuário com sucesso
    Dado que já existe um usuário com e-mail "deletar@example.com"
    Quando um usuário envia uma requisição DELETE para a URL do usuário com e-mail "deletar@example.com"
    Então o status da resposta deve ser 204
    E um usuário com e-mail "deletar@example.com" não deve existir no banco de dados

