# Gestão Pessoal

App Android de gestão pessoal, em Kotlin e Jetpack Compose.

## Funcionalidades

### Lembretes
- Cadastrar, editar, concluir e excluir lembretes (título, descrição, data e hora).
- Repetição: não repetir, diariamente, semanalmente ou mensalmente.
- Notificação no horário, com a ação **Concluir** direto na notificação.
- Os alarmes são recriados automaticamente depois de reiniciar o aparelho ou atualizar o app.
- Avisos na tela quando faltam as permissões de notificação ou de "Alarmes e lembretes".

## Arquitetura

```
app/src/main/java/com/gestaopessoal/app/
├── GestaoPessoalApp.kt        # Application + AppContainer (injeção de dependências manual)
├── MainActivity.kt
├── data/                      # Room: entidade, DAO, banco e repositório
├── notification/              # AlarmManager, receivers e montagem das notificações
└── ui/                        # Telas Compose, ViewModels, navegação e tema
```

- **Persistência:** Room (`gestao_pessoal.db`). O schema é exportado para `app/schemas/`; faça commit dessa pasta quando ela for gerada, para facilitar migrações futuras.
- **Agendamento:** `AlarmManager.setExactAndAllowWhileIdle`. Se o usuário negar alarmes exatos (Android 12+), o app usa um alarme aproximado.
- **Fluxo:** toda alteração passa pelo `ReminderRepository`, que mantém banco e alarmes em sincronia.

Requisitos: Android 8.0 (API 26) ou superior; compila com SDK 35 e JDK 17.

## Como rodar

Abra o projeto no Android Studio e rode o módulo `app` num emulador ou aparelho.

Testes unitários:

```bash
./gradlew testDebugUnitTest
```

O GitHub Actions roda os testes e o `assembleDebug` a cada push/PR.

### Testando as notificações
1. Crie um lembrete para 1–2 minutos à frente.
2. Feche o app (ou bloqueie a tela) e aguarde a notificação.
3. Para testar a recriação após reinício: `adb reboot` com um lembrete futuro cadastrado.

## Próximos passos (ideias)
- Abrir o lembrete direto ao tocar na notificação.
- Adiar ("soneca") pela notificação.
- Categorias/etiquetas e busca.
- Novos módulos: tarefas, hábitos, finanças.
