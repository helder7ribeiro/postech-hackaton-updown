# language: pt
Funcionalidade: Armazenamento de Vídeos no S3
  Como um sistema de processamento, eu quero armazenar arquivos de vídeo
  para que eles possam ser processados posteriormente.

  Cenário: Armazenar um vídeo com sucesso
    Dado que o serviço S3 está configurado e operacional
    Quando um usuário envia um vídeo chamado "meu_video.mp4" do tipo "video/mp4"
    Então o vídeo deve ser salvo no S3
    E uma URI do S3 deve ser retornada

  Cenário: Falha ao armazenar um vídeo devido a um erro de leitura do arquivo
    Dado que o serviço S3 está configurado e operacional
    Quando um usuário tenta enviar um vídeo com um stream de dados corrompido
    Então uma FalhaInfraestruturaException deve ser lançada
