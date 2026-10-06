# Gestão Pessoal

App Android em Kotlin e Jetpack Compose.

Neste primeiro momento a tela inicial mostra **Hello World**.

## Como validar

O texto da tela é coberto por um teste unitário (Robolectric + Compose UI Test). Ele sobe a tela em JVM e falha se "Hello World" não aparecer.

```bash
./gradlew testDebugUnitTest
```

O mesmo comando roda no GitHub Actions a cada push/PR, junto com `assembleDebug`.

Para ver o app no emulador ou no aparelho, abra o projeto no Android Studio e rode o módulo `app`.
