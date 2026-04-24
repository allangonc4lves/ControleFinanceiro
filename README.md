# Zeno - Controle Financeiro 

📱 Preview <br> (APP EM DESENVOLVIMENTO...)<br>

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/5d4cc845-f62a-48ab-9495-947abcb321ba" width="250"/></td>
    <td><img src="https://github.com/user-attachments/assets/0a7e0e19-2cc0-459f-9c1a-7f4af79cc5bf" width="250"/></td>
    <td><img src="https://github.com/user-attachments/assets/58e55e1d-259a-4206-b5fa-98e239a1f090" width="250"/></td>
  </tr>
</table>

🚀 Funcionalidades
📊 Cadastro e acompanhamento de receitas e despesas
📅 Histórico financeiro organizado por data
📈 Visualização clara do saldo atual
🔐 Autenticação de usuários (login/registro)
☁️ Sincronização em tempo real com a nuvem
📴 Suporte offline com sincronização automática

🧠 Tecnologias e Arquitetura
Este projeto será construído utilizando tecnologias modernas e recomendadas pelo Google:

Jetpack Compose → UI declarativa e reativa
Hilt (DI) → Injeção de dependência
Room → Persistência local (offline-first)
Firebase Auth → Autenticação segura
Firebase Firestore → Banco de dados em tempo real
Kotlin Coroutines → Programação assíncrona
ViewModel + Lifecycle → Gerenciamento de estado e ciclo de vida
🏗️ Arquitetura

O projeto segue o padrão MVVM + Clean Architecture:

📦 data
 ├── local (Room)
 ├── remote (Firebase)
 └── repository

📦 domain
 └── usecases

📦 presentation
 ├── ui (Compose)
 ├── viewmodel

 📦 di
 ├── (Hilt)

✔ Separação clara de responsabilidades
✔ Código escalável e testável
✔ Fácil manutenção

⚡ Destaques Técnicos
🔄 Offline-first com sincronização automática (Room + Firestore)
🔥 Realtime updates com Firestore
🧩 Modularização lógica (camadas)
🚀 Uso de StateFlow / LiveData para UI reativa
🧼 Código limpo seguindo boas práticas

📦 Como rodar o projeto
git clone https://github.com/allangonc4lves/ControleFinanceiro.git
Abra no Android Studio
Configure o Firebase (google-services.json)
Execute o app
🔐 Configuração do Firebase
Crie um projeto no Firebase
Ative:
Authentication (Email/Senha)
Firestore Database
Adicione o arquivo google-services.json no projeto

🎯 Objetivo do Projeto
Este app foi desenvolvido com foco em:
- Praticar desenvolvimento Android moderno
- Demonstrar domínio de arquitetura escalável
- Simular um app real pronto para produção

👨‍💻 Autor
Desenvolvido por Allan
