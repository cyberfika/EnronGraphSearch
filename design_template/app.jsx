// app.jsx — Graph Search workspace shell.

const { useState, useMemo, useEffect } = React;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "theme": "dark",
  "accentHue": 75,
  "density": "regular",
  "layout": "left",
  "fontDisplay": "Instrument Serif"
}/*EDITMODE-END*/;

const TABS = [
  { id: "overview",  num: "01", label: "VISÃO GERAL" },
  { id: "path",      num: "02", label: "DFS · BFS" },
  { id: "distance",  num: "03", label: "DISTÂNCIA D" },
  { id: "critical",  num: "04", label: "CAMINHO CRÍTICO" },
];

function Sidebar({ graph, onSelectEmail, activeEmail }) {
  // Show top emails by combined degree as a quick directory
  const directory = useMemo(() => {
    return graph.vertices
      .map(v => ({
        email: v.email, label: v.label,
        deg: v.in.length + v.out.length,
      }))
      .sort((a, b) => b.deg - a.deg || a.email.localeCompare(b.email));
  }, [graph]);

  const totalWeight = useMemo(() => {
    let t = 0;
    for (const m of graph.adj.values()) for (const a of m.values()) t += a.peso;
    return t;
  }, [graph]);

  return (
    <aside className="sidebar">
      <div className="side-section">
        <div className="side-h"><span>FONTE</span><span className="side-h-num">corpus</span></div>
        <dl className="kv">
          <dt>Dataset</dt><dd>enron <small>v1.0</small></dd>
          <dt>Mensagens</dt><dd>{totalWeight.toLocaleString("pt-BR")}</dd>
          <dt>Janela</dt><dd>1999—2002</dd>
        </dl>
        <div className="dataset-card">
          <div className="name"><span>enron-public.maildir</span><span className="badge">ATIVO</span></div>
          <div className="meta">grafo construído · {graph.numVertices} vértices · {graph.numArestas} arestas</div>
        </div>
      </div>
      <div className="side-section">
        <div className="side-h"><span>RESUMO</span><span className="side-h-num">live</span></div>
        <dl className="kv">
          <dt>|V|</dt><dd>{graph.numVertices}</dd>
          <dt>|E|</dt><dd>{graph.numArestas}</dd>
          <dt>Σ pesos</dt><dd>{totalWeight}</dd>
          <dt>Densidade</dt><dd>{((graph.numArestas / (graph.numVertices * (graph.numVertices - 1))) * 100).toFixed(2)}<small>%</small></dd>
        </dl>
      </div>
      <div className="side-section">
        <div className="side-h"><span>DIRETÓRIO</span><span className="side-h-num">{directory.length}</span></div>
        <div className="side-list">
          {directory.map((v, i) => (
            <div
              key={v.email}
              className="side-list-row"
              onClick={() => onSelectEmail?.(v.email)}
              style={activeEmail === v.email ? {
                background: "var(--bg-2)", color: "var(--accent)"
              } : null}
            >
              <span className="em">
                {v.email.split("@")[0]}
                <span className="role">{v.label}</span>
              </span>
              <span className="num">{v.deg}</span>
            </div>
          ))}
        </div>
      </div>
    </aside>
  );
}

function StatusBar({ graph, tab }) {
  const [now, setNow] = useState(new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 30000);
    return () => clearInterval(id);
  }, []);
  const t = now.toTimeString().slice(0, 8);
  return (
    <div className="statusbar">
      <span className="seg"><span className="live" /> ANALISADOR · <b>online</b></span>
      <span className="seg">JVM <b>17.0.10</b></span>
      <span className="seg">grafo <b>{graph.numVertices}/{graph.numArestas}</b></span>
      <span className="seg">painel · <b>{tab}</b></span>
      <span className="spacer" />
      <span className="seg">br.edu.enron.app.Main</span>
      <span className="seg">UTC <b>{t}</b></span>
    </div>
  );
}

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const [tab, setTab] = useState("overview");
  const [activeEmail, setActiveEmail] = useState(null);

  const graph = useMemo(
    () => GrafoContatos.buildGraph(window.ENRON_PEOPLE, window.ENRON_EDGES),
    []
  );

  // Apply theme + density + accent
  useEffect(() => {
    const root = document.documentElement;
    root.dataset.theme = t.theme;
    root.dataset.density = t.density;
    root.dataset.layout = t.layout;
    root.style.setProperty("--accent",
      `oklch(${t.theme === "light" ? 0.62 : 0.82} ${t.theme === "light" ? 0.16 : 0.15} ${t.accentHue})`);
    root.style.setProperty("--accent-2",
      `oklch(${t.theme === "light" ? 0.55 : 0.92} ${t.theme === "light" ? 0.18 : 0.10} ${t.accentHue + 5})`);
    root.style.setProperty("--accent-ink",
      t.theme === "light"
        ? "oklch(0.99 0.005 90)"
        : `oklch(0.20 0.05 ${t.accentHue})`);
    root.style.setProperty("--ff-display", `'${t.fontDisplay}', Georgia, serif`);
  }, [t]);

  const Panel = {
    overview: <OverviewPanel graph={graph} onSelectEmail={(e) => { setActiveEmail(e); }} />,
    path:     <PathPanel graph={graph} />,
    distance: <DistancePanel graph={graph} />,
    critical: <CriticalPanel graph={graph} />,
  }[tab];

  return (
    <div className="shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark"><span /></span>
          <span className="brand-name">
            <b>GRAPH</b><i>·</i><b>SEARCH</b>
          </span>
        </div>
        <nav className="tabs">
          {TABS.map(x => (
            <button
              key={x.id}
              className="tab"
              aria-current={tab === x.id ? "true" : "false"}
              onClick={() => setTab(x.id)}
            >
              <span className="tab-num">{x.num}</span>
              <span>{x.label}</span>
            </button>
          ))}
        </nav>
        <span className="topbar-spacer" />
        <div className="topbar-meta">
          <span><span className="dot" /> ANALISADOR DE CONTATOS · ENRON</span>
        </div>
      </header>

      <Sidebar
        graph={graph}
        activeEmail={activeEmail}
        onSelectEmail={(em) => { setActiveEmail(em); }}
      />

      <main className="main" key={tab}>{Panel}</main>

      <StatusBar graph={graph} tab={TABS.find(x => x.id === tab)?.label || "—"} />

      <TweaksPanel title="Tweaks">
        <TweakSection label="Tema" />
        <TweakRadio
          label="Modo"
          value={t.theme}
          options={["dark", "light"]}
          onChange={(v) => setTweak("theme", v)}
        />
        <TweakSlider
          label="Matiz do acento"
          value={t.accentHue} min={0} max={360} step={1}
          unit="°"
          onChange={(v) => setTweak("accentHue", v)}
        />

        <TweakSection label="Densidade & layout" />
        <TweakRadio
          label="Densidade"
          value={t.density}
          options={["compact", "regular", "comfortable"]}
          onChange={(v) => setTweak("density", v)}
        />
        <TweakRadio
          label="Sidebar"
          value={t.layout}
          options={["left", "right"]}
          onChange={(v) => setTweak("layout", v)}
        />

        <TweakSection label="Tipografia" />
        <TweakSelect
          label="Fonte de display"
          value={t.fontDisplay}
          options={[
            "Instrument Serif",
            "Inter Tight",
            "JetBrains Mono",
          ]}
          onChange={(v) => setTweak("fontDisplay", v)}
        />
      </TweaksPanel>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
