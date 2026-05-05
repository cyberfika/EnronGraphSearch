# Investigação de Diferenças de Aparência: Windows vs Mac

## Passo 1: Executar Diagnóstico

### Windows
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="br.edu.enron.app.DebugLauncher" > diagnostics_windows.txt 2>&1
```

### Mac
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="br.edu.enron.app.DebugLauncher" > diagnostics_mac.txt 2>&1
```

Abra os arquivos `.txt` gerados e **compare lado a lado**.

---

## Passo 2: Procure por Diferenças Críticas

### Fontes

**O que procurar:**
- Windows: `Font.MONOSPACED` mapeia para `Courier New`
- Mac: `Font.MONOSPACED` mapeia para `Monaco` ou `Menlo`

Se forem diferentes, esta é a **causa principal**.

**Solução:**
Mude `SearchPanel.java` para usar fontes específicas:

```java
// ANTES (problemático):
private static final Font MONO_MD = new Font(Font.MONOSPACED, Font.PLAIN, 12);

// DEPOIS (explícito):
private static final Font MONO_MD = new Font("Consolas", Font.PLAIN, 12);  // Windows
// OU:
private static final Font MONO_MD = new Font("Monaco", Font.PLAIN, 12);    // Mac
```

### Screen DPI

**O que procurar:**
- Windows: `96 DPI` ou `120 DPI`
- Mac: `72 DPI` ou `144 DPI` (retina)

Se forem muito diferentes, o escalonamento pode ser o culpado.

---

## Passo 3: Screenshot Comparison

1. Rode a aplicação completa em **Windows**
2. Rode a aplicação completa em **Mac**
3. Capture a mesma aba em ambos
4. **Compare:**
   - Tamanho do texto
   - Espaçamento (padding/margins)
   - Espessura de bordas
   - Cores (anti-aliasing pode fazer parecer diferente)
   - Alinhamento de componentes

---

## Passo 4: Comparação de Métricas de Texto

O `compareTextMetrics()` abre uma pequena janela com 3 linhas de texto em diferentes fontes.

**Medir:**
- Altura das linhas
- Espaço entre linhas
- Largura do texto

Se forem muito diferentes, o **kerning** ou **line-height** é o culpado.

---

## Possíveis Culpados (por frequência)

| Culpado | Windows | Mac | Solução |
|---------|---------|-----|---------|
| **Fonte padrão** | Courier New | Monaco | Use fonte explícita |
| **DPI** | 96 ou 120 | 72 ou 144 | Escale com `Toolkit.getScreenResolution()` |
| **Line-height** | Maior | Menor | Adicione `getLineMetrics()` |
| **Rendering** | ClearType | Quartz | Use `RenderingHints` |
| **Anti-aliasing** | Mais nítido | Mais suave | Force com `RenderingHints.KEY_TEXT_ANTIALIASING` |

---

## Passo 5: Caso Nenhuma Solução Funcione

Se as diferenças persistirem mesmo com fontes explícitas:

1. **Capture screenshots** com anotações
2. **Meça em pixels** (use DevTools ou ferramentas de screenshot)
3. **Documente as diferenças específicas** (ex: "badges em Mac têm 3px de margem a mais")
4. Considere usar `UIManager.getLookAndFeel()` customizado

---

## Arquivo de Saída Esperado

```
======================================================================
RENDERING DIAGNOSTICS
======================================================================

[SYSTEM]
  OS Name:     Windows 10
  OS Version:  10.0
  OS Arch:     x86_64
  Java Version:17.0.10

[SCREEN]
  Screen DPI:  96
  Screen Size: 1920x1080

[FONTS]
  Font.MONOSPACED:
    → Actual family: Courier New
    → Font name:     Courier New
  Font.SERIF:
    → Actual family: Times New Roman
    ...

[AVAILABLE FONTS ON THIS SYSTEM]
  ✓ Consolas
  ✓ Courier New
  ✗ Monaco (NOT FOUND)
  ✓ Menlo
  ...

[TEXT METRICS COMPARISON]
  Courier New @ 12px: height=14, ascent=11, descent=3, width=110.2
  ...
```

---

## Próximas Etapas

Após executar o diagnóstico em ambos os SOs, **compartilhe os arquivos** gerados ou **descreva as diferenças principais**. Com isso, posso ajudar a corrigir de forma específica.
