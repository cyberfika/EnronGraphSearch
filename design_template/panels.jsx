// panels.jsx — feature panels for the Graph Search workspace.
// Exports: OverviewPanel, PathPanel, DistancePanel, CriticalPanel, EmailInput

const { useState, useEffect, useRef, useMemo } = React;

/* ───── Email autocomplete input ─────────────────────────────────────── */
function EmailInput({ value, onChange, graph, label, accent, placeholder = "ex.: kenneth.lay@enron.com" }) {
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const ref = useRef(null);

  const matches = useMemo(() => {
    const q = (value || "").trim().toLowerCase();
    if (!q) return graph.vertices.slice(0, 8);
    return graph.vertices
      .filter(v => v.email.toLowerCase().includes(q) || (v.label || "").toLowerCase().includes(q))
      .slice(0, 12);
  }, [value, graph]);

  useEffect(() => {
    function onClick(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const pick = (v) => { onChange(v.email); setOpen(false); };

  return (
    <div className="q-cell" ref={ref}>
      <div className="lbl">
        {label} <b style={{ marginLeft: 6 }}>{accent}</b>
      </div>
      <input
        className="q-input"
        value={value}
        placeholder={placeholder}
        onChange={(e) => { onChange(e.target.value); setOpen(true); setActive(0); }}
        onFocus={() => setOpen(true)}
        onKeyDown={(e) => {
          if (e.key === "ArrowDown") { setActive(a => Math.min(matches.length - 1, a + 1)); e.preventDefault(); }
          else if (e.key === "ArrowUp") { setActive(a => Math.max(0, a - 1)); e.preventDefault(); }
          else if (e.key === "Enter" && open && matches[active]) { pick(matches[active]); e.preventDefault(); }
          else if (e.key === "Escape") setOpen(false);
        }}
      />
      {open && matches.length > 0 && (
        <div className="q-suggest">
          {matches.map((v, i) => (
            <button
              key={v.email}
              className={i === active ? "active" : ""}
              onMouseEnter={() => setActive(i)}
              onClick={() => pick(v)}
            >
              <span className="em">{v.email}</span>
              <span className="role">{v.label}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

/* ───── Overview / leaderboards ──────────────────────────────────────── */
function OverviewPanel({ graph, onSelectEmail }) {
  const totalWeight = useMemo(() => {
    let t = 0;
    for (const m of graph.adj.values()) for (const a of m.values()) t += a.peso;
    return t;
  }, [graph]);
  const avgDeg = (graph.numArestas / Math.max(graph.numVertices, 1)).toFixed(2);
  const density = ((graph.numArestas / (graph.numVertices * (graph.numVertices - 1))) * 100).toFixed(2);

  const topOut = useMemo(() => GrafoContatos.topGrauSaida(graph, 20), [graph]);
  const topIn  = useMemo(() => GrafoContatos.topGrauEntrada(graph, 20), [graph]);

  const maxOut = topOut[0]?.grau || 1;
  const maxIn  = topIn[0]?.grau || 1;

  return (
    <>
      <div className="panel-header">
        <div className="panel-h-left">
          <div className="panel-eyebrow">
            <span className="sep" /> §01 · visão geral · grafo direcionado · ponderado · rotulado
          </div>
          <h1 className="panel-title">A topologia <em>de uma corporação</em>, em arestas.</h1>
          <div className="panel-sub">
            Cada vértice é um indivíduo identificado por seu e-mail. Cada aresta é um envio. O peso é a frequência. Os algoritmos de percurso desta tela são adaptados de Dijkstra, BFS e DFS — todos com tratamento explícito de ciclos.
          </div>
        </div>
        <div className="panel-h-right">
          <span className="pill">DIRECIONADO</span>
          <span className="pill">PONDERADO</span>
          <span className="pill">ROTULADO</span>
        </div>
      </div>

      <div className="panel-body">
        <div className="stat-grid">
          <div className="stat-cell">
            <span className="label">Vértices</span>
            <span className="value">{graph.numVertices}<sup>indivíduos</sup></span>
            <span className="note">getNumeroVertices()</span>
          </div>
          <div className="stat-cell">
            <span className="label">Arestas</span>
            <span className="value">{graph.numArestas}<sup>relações</sup></span>
            <span className="note">getNumeroArestas()</span>
          </div>
          <div className="stat-cell">
            <span className="label">Mensagens (Σ pesos)</span>
            <span className="value">{totalWeight.toLocaleString("pt-BR")}</span>
            <span className="note">soma das frequências</span>
          </div>
          <div className="stat-cell accent">
            <span className="label">Densidade</span>
            <span className="value">{density}<sup>%</sup></span>
            <span className="note">grau médio · <b>{avgDeg}</b></span>
          </div>
        </div>

        <div className="two-col">
          <div className="col">
            <div className="col-h">
              <span className="title">↗ Top 20 · grau de saída</span>
              <span className="desc">remetentes mais ativos</span>
            </div>
            {topOut.map((r, i) => (
              <div key={r.email} className={`lb-row ${i === 0 ? "top1" : ""}`} onClick={() => onSelectEmail?.(r.email)}>
                <span className="rank">{String(i + 1).padStart(2, "0")}</span>
                <span className="who">
                  <span className="em">{r.email}</span>
                  <span className="role">{r.label}</span>
                </span>
                <span className="grau">{r.grau}</span>
                <span className="bar"><i style={{ right: `${(1 - r.grau / maxOut) * 100}%` }} /></span>
              </div>
            ))}
          </div>
          <div className="col">
            <div className="col-h">
              <span className="title">↙ Top 20 · grau de entrada</span>
              <span className="desc">destinatários mais procurados</span>
            </div>
            {topIn.map((r, i) => (
              <div key={r.email} className={`lb-row ${i === 0 ? "top1" : ""}`} onClick={() => onSelectEmail?.(r.email)}>
                <span className="rank">{String(i + 1).padStart(2, "0")}</span>
                <span className="who">
                  <span className="em">{r.email}</span>
                  <span className="role">{r.label}</span>
                </span>
                <span className="grau">{r.grau}</span>
                <span className="bar"><i style={{ right: `${(1 - r.grau / maxIn) * 100}%` }} /></span>
              </div>
            ))}
          </div>
        </div>

        <div className="note-block">
          <b>Critério de ordenação. </b>Empates são resolvidos alfabeticamente pelo e-mail, em ordem crescente. O grau considera a quantidade de vizinhos distintos (não a soma dos pesos).
        </div>
      </div>
    </>
  );
}

/* ───── Path panel (DFS / BFS) ───────────────────────────────────────── */
function PathPanel({ graph }) {
  const [src, setSrc] = useState("kenneth.lay@enron.com");
  const [dst, setDst] = useState("matthew.lenhart@enron.com");
  const [algo, setAlgo] = useState("BFS");
  const [result, setResult] = useState(null);

  function run() {
    const fn = algo === "BFS" ? GrafoContatos.bfs : GrafoContatos.dfs;
    const r = fn(graph, src.trim().toLowerCase(), dst.trim().toLowerCase());
    setResult({ ...r, algo, src, dst, ts: Date.now() });
  }

  useEffect(() => { run(); /* eslint-disable-next-line */ }, []);

  const edges = useMemo(() => {
    if (!result?.path?.length || result.path.length < 2) return [];
    const out = [];
    for (let i = 0; i < result.path.length - 1; i++) {
      out.push(GrafoContatos.arestaEntre(graph, result.path[i], result.path[i + 1]));
    }
    return out;
  }, [graph, result]);

  const totalWeight = edges.reduce((s, e) => s + (e?.peso || 0), 0);
  const maxW = Math.max(1, ...edges.map(e => e?.peso || 0));

  return (
    <>
      <div className="panel-header">
        <div className="panel-h-left">
          <div className="panel-eyebrow">
            <span className="sep" /> §02 · percurso · busca em largura · busca em profundidade
          </div>
          <h1 className="panel-title">X alcança Y? <em>Em qual rota.</em></h1>
          <div className="panel-sub">
            Verifique a conectividade entre dois indivíduos e visualize a rota percorrida. BFS retorna o caminho com menor número de arestas; DFS retorna a primeira rota encontrada explorando em profundidade. Ambos tratam ciclos por meio de conjunto de visitados.
          </div>
        </div>
        <div className="panel-h-right">
          <span className="pill">{result?.algo || algo}</span>
          <span className="pill">CICLOS · TRATADOS</span>
        </div>
      </div>

      <div className="panel-body">
        <div className="query">
          <EmailInput graph={graph} value={src} onChange={setSrc} label="Origem" accent="X" />
          <EmailInput graph={graph} value={dst} onChange={setDst} label="Destino" accent="Y" />
          <div className="q-cell" style={{ padding: "var(--s-3) var(--s-4)" }}>
            <div className="lbl">Algoritmo</div>
            <div className="algo-toggle" style={{ height: 28, marginTop: 4 }}>
              <button aria-pressed={algo === "BFS"} onClick={() => setAlgo("BFS")}>BFS</button>
              <button aria-pressed={algo === "DFS"} onClick={() => setAlgo("DFS")}>DFS</button>
            </div>
          </div>
          <button className="btn primary" onClick={run}>Executar <span className="arrow" /></button>
        </div>

        {result && result.path.length === 0 && (
          <div className="result-empty">
            <b>Sem caminho.</b>
            Não existe rota direcionada de <code>{result.src}</code> até <code>{result.dst}</code>.
            <div style={{ marginTop: 8 }}>visitados: {result.visited} vértices</div>
          </div>
        )}

        {result && result.path.length > 0 && (
          <>
            <div className="result">
              <div className="result-main">
                <div className="result-h">
                  <span className="label">{result.algo} · caminho encontrado</span>
                  <span className="verdict">alcançado.</span>
                </div>
                <div className="meta-line">
                  <span><b>{result.path.length}</b> nós</span>
                  <span><b>{Math.max(result.path.length - 1, 0)}</b> arestas</span>
                  <span><b>{result.visited}</b> visitados</span>
                  <span>Σ pesos · <b>{totalWeight}</b></span>
                </div>

                <div className="chain">
                  {result.path.map((email, i) => {
                    const v = graph.byEmail[email];
                    const isStart = i === 0;
                    const isEnd = i === result.path.length - 1;
                    const ed = edges[i];
                    return (
                      <div key={email} className="chain-step">
                        <div className="chain-rail">
                          <div className={`node ${isStart ? "start" : ""} ${isEnd ? "end" : ""}`} />
                          {!isEnd && <div className="line" />}
                        </div>
                        <div className="chain-body">
                          <div>
                            <span className="em">{email}</span>
                            <span className="role">{v?.label}</span>
                          </div>
                          {ed && (
                            <div className="edge">
                              <span>↓ envia</span>
                              <span className="micro"><i style={{ right: `${(1 - ed.peso / maxW) * 100}%` }} /></span>
                              <span className="w">{ed.peso}</span>
                              <span>msgs</span>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              <div className="result-side">
                <div className="side-stats">
                  <div className="side-stat">
                    <span className="l">Algoritmo</span>
                    <span className="v mono accent">{result.algo}</span>
                    <span className="sub">{result.algo === "BFS" ? "fila · explora por níveis · favorece menor número de arestas" : "pilha · explora em profundidade · primeira rota encontrada"}</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Comprimento</span>
                    <span className="v">{Math.max(result.path.length - 1, 0)}</span>
                    <span className="sub">arestas no caminho</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Custo das arestas</span>
                    <span className="v mono">{totalWeight.toLocaleString("pt-BR")}</span>
                    <span className="sub">soma das frequências dos envios</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Vértices visitados</span>
                    <span className="v mono">{result.visited}</span>
                    <span className="sub">marcados antes de encontrar o destino</span>
                  </div>
                </div>
              </div>
            </div>

            <PathViz graph={graph} path={result.path} edges={edges} channelHue={75} />
          </>
        )}
      </div>
    </>
  );
}

/* ───── Distance D ───────────────────────────────────────────────────── */
function DistancePanel({ graph }) {
  const [src, setSrc] = useState("jeffrey.skilling@enron.com");
  const [d, setD] = useState(2);
  const [result, setResult] = useState(null);

  // Tier counts (BFS by levels) — for the rings viz
  const tiers = useMemo(() => {
    const out = [];
    for (let i = 0; i <= 5; i++) {
      out.push(GrafoContatos.distanciaD(graph, src.trim().toLowerCase(), i).length);
    }
    return out;
  }, [graph, src]);

  function run() {
    const r = GrafoContatos.distanciaD(graph, src.trim().toLowerCase(), d);
    setResult({ list: r, src, d });
  }
  useEffect(() => { run(); /* eslint-disable-next-line */ }, []);

  // Ring viz
  const rings = [];
  const cx = 140, cy = 130;
  const radii = [0, 30, 56, 82, 108];
  for (let i = 0; i < radii.length; i++) {
    rings.push({ r: radii[i], i });
  }

  return (
    <>
      <div className="panel-header">
        <div className="panel-h-left">
          <div className="panel-eyebrow">
            <span className="sep" /> §03 · distância · BFS por níveis
          </div>
          <h1 className="panel-title">Quem está <em>exatamente a D arestas</em> daqui.</h1>
          <div className="panel-sub">
            Busca em largura por níveis a partir de N. Retorna apenas vértices a distância exata D — nem antes, nem depois — em ordem alfabética.
          </div>
        </div>
        <div className="panel-h-right">
          <span className="pill">D = {d}</span>
        </div>
      </div>

      <div className="panel-body">
        <div className="query three">
          <EmailInput graph={graph} value={src} onChange={setSrc} label="Nó N" accent="origem" />
          <div className="q-cell">
            <div className="lbl">Distância <b>D</b></div>
            <div className="dist-stepper" style={{ marginTop: 4 }}>
              <button onClick={() => setD(v => Math.max(0, v - 1))}>−</button>
              <span className="val">{d}</span>
              <button onClick={() => setD(v => Math.min(8, v + 1))}>+</button>
            </div>
          </div>
          <button className="btn primary" onClick={run}>Computar <span className="arrow" /></button>
        </div>

        <div className="dist-result">
          <div className="dist-list">
            <div className="head">
              <span className="label">Vértices a distância {result?.d ?? d}</span>
              <span className="count">{result?.list.length ?? 0}</span>
            </div>
            {result && result.list.length === 0 && (
              <div className="result-empty" style={{ border: 0, padding: "var(--s-6) 0" }}>
                <b>Nenhum vértice nessa camada.</b>
                Tente outro D, ou verifique se o nó está corretamente conectado.
              </div>
            )}
            {result && result.list.length > 0 && (
              <div className="dist-grid">
                {result.list.map(em => (
                  <div key={em} className="item">
                    <span className="dot" />
                    <span>{em}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
          <div className="dist-rings">
            <svg width="280" height="270" viewBox="0 0 280 270">
              {rings.slice().reverse().map(({ r, i }) => (
                <circle key={i}
                  cx={cx} cy={cy} r={Math.max(r, 3)}
                  fill="none"
                  stroke={i === d ? "var(--accent)" : "var(--rule)"}
                  strokeWidth={i === d ? 1 : 0.5}
                  strokeDasharray={i === d ? "none" : "2 3"}
                />
              ))}
              <circle cx={cx} cy={cy} r="3" fill="var(--ink)" />
              <text x={cx} y={cy - 8} textAnchor="middle"
                fontFamily="var(--ff-mono)" fontSize="9" fill="var(--muted)"
                style={{ letterSpacing: "0.04em", textTransform: "uppercase" }}>N</text>
            </svg>
            <div className="legend">
              {tiers.map((n, i) => (
                <div key={i} className={`row ${i === d ? "active" : ""}`}>
                  <span className="sw" />
                  <span>D = {i}</span>
                  <span className="n">{n}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="note-block">
          <b>Definição. </b>Distância é medida em arestas direcionadas. Uma ligação direta de N → V tem distância 1. D = 0 retorna o próprio N. Ciclos são tratados marcando vértices visitados a cada nível.
        </div>
      </div>
    </>
  );
}

/* ───── Critical path (inverse-weight Dijkstra) ──────────────────────── */
function CriticalPanel({ graph }) {
  const [src, setSrc] = useState("phillip.allen@enron.com");
  const [dst, setDst] = useState("vince.kaminski@enron.com");
  const [result, setResult] = useState(null);

  function run() {
    const r = GrafoContatos.caminhoCritico(graph, src.trim().toLowerCase(), dst.trim().toLowerCase());
    setResult({ ...(r || {}), src, dst });
  }
  useEffect(() => { run(); /* eslint-disable-next-line */ }, []);

  const dep = result?.dependencia || 0;
  const maxW = Math.max(1, ...(result?.edges?.map(e => e?.peso || 0) || [1]));

  return (
    <>
      <div className="panel-header">
        <div className="panel-h-left">
          <div className="panel-eyebrow">
            <span className="sep" /> §04 · caminho crítico · Dijkstra adaptado · custo = 1 / peso
          </div>
          <h1 className="panel-title">O canal <em>de maior dependência</em> entre A e C.</h1>
          <div className="panel-sub">
            Adaptação do Dijkstra usando o inverso do peso da aresta. Como peso alto representa relação forte, custo baixo (1/peso) faz com que o algoritmo prefira atravessar laços densos. O caminho retornado é o de maior dependência acumulada — o canal mais provável para o fluxo de informação.
          </div>
        </div>
        <div className="panel-h-right">
          <span className="pill">CUSTO = 1 / PESO</span>
        </div>
      </div>

      <div className="panel-body">
        <div className="query">
          <EmailInput graph={graph} value={src} onChange={setSrc} label="Indivíduo" accent="A" />
          <EmailInput graph={graph} value={dst} onChange={setDst} label="Indivíduo" accent="C" />
          <div className="q-cell" style={{ padding: "var(--s-3) var(--s-4)" }}>
            <div className="lbl">Adaptação</div>
            <div style={{
              fontFamily: "var(--ff-mono)", fontSize: 13, color: "var(--ink)",
              marginTop: 6
            }}>
              min Σ <span style={{ color: "var(--accent)" }}>1/peso</span>
            </div>
          </div>
          <button className="btn primary" onClick={run}>Calcular <span className="arrow" /></button>
        </div>

        {result && (!result.path || result.path.length === 0) && (
          <div className="result-empty">
            <b>Sem canal entre A e C.</b>
            Não existe caminho direcionado conectando os dois indivíduos.
          </div>
        )}

        {result && result.path && result.path.length > 0 && (
          <>
            <div className="result">
              <div className="result-main">
                <div className="result-h">
                  <span className="label">caminho crítico aproximado</span>
                  <span className="verdict">canal identificado.</span>
                </div>
                <div className="meta-line">
                  <span><b>{result.path.length}</b> nós</span>
                  <span><b>{result.edges.length}</b> arestas</span>
                  <span>custo inverso · <b>{result.custoInverso.toFixed(4)}</b></span>
                  <span>dependência · <b style={{ color: "var(--accent)" }}>{dep}</b></span>
                </div>

                <div className="chain">
                  {result.path.map((email, i) => {
                    const v = graph.byEmail[email];
                    const isStart = i === 0;
                    const isEnd = i === result.path.length - 1;
                    const ed = result.edges[i];
                    return (
                      <div key={email} className="chain-step">
                        <div className="chain-rail">
                          <div className={`node ${isStart ? "start" : ""} ${isEnd ? "end" : ""}`} />
                          {!isEnd && <div className="line" />}
                        </div>
                        <div className="chain-body">
                          <div>
                            <span className="em">{email}</span>
                            <span className="role">{v?.label}</span>
                          </div>
                          {ed && (
                            <div className="edge">
                              <span>peso original</span>
                              <span className="micro"><i style={{ right: `${(1 - ed.peso / maxW) * 100}%` }} /></span>
                              <span className="w">{ed.peso}</span>
                              <span>· custo {(1 / ed.peso).toFixed(4)}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>

                <div className="crit-flow">
                  <div className="crit-flow-h">
                    <span>distribuição da dependência acumulada</span>
                    <span>Σ = {dep}</span>
                  </div>
                  <div className="crit-flow-bar">
                    {result.edges.map((e, i) => {
                      const pct = (e.peso / dep) * 100;
                      const hue = 75 - i * 8;
                      return (
                        <div key={i} style={{
                          width: `${pct}%`,
                          background: `oklch(0.82 0.15 ${hue})`,
                        }}>{e.peso}</div>
                      );
                    })}
                  </div>
                  <div className="crit-flow-labels">
                    {result.edges.map((e, i) => {
                      const pct = (e.peso / dep) * 100;
                      const a = result.path[i].split("@")[0];
                      const b = result.path[i + 1].split("@")[0];
                      return (
                        <div key={i} style={{ width: `${pct}%` }}>
                          {a} → {b}
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>

              <div className="result-side">
                <div className="side-stats">
                  <div className="side-stat">
                    <span className="l">Dependência acumulada</span>
                    <span className="v accent">{dep}</span>
                    <span className="sub">soma dos pesos originais</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Custo inverso (Σ 1/peso)</span>
                    <span className="v mono">{result.custoInverso.toFixed(4)}</span>
                    <span className="sub">métrica minimizada pelo Dijkstra</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Aresta mais forte</span>
                    <span className="v mono accent">
                      {Math.max(...result.edges.map(e => e.peso))}
                    </span>
                    <span className="sub">maior frequência no caminho</span>
                  </div>
                  <div className="side-stat">
                    <span className="l">Aresta mais fraca</span>
                    <span className="v mono">
                      {Math.min(...result.edges.map(e => e.peso))}
                    </span>
                    <span className="sub">gargalo do canal</span>
                  </div>
                </div>
              </div>
            </div>

            <PathViz graph={graph} path={result.path} edges={result.edges} channelHue={45} />
          </>
        )}

        <div className="note-block">
          <b>Por que inverter o peso. </b>O Dijkstra clássico minimiza custo. Para favorecer relações de alta frequência, transformamos peso alto em custo baixo via 1/peso. Assim, o caminho de menor custo no espaço transformado corresponde ao caminho de maior dependência no espaço original.
        </div>
      </div>
    </>
  );
}

window.OverviewPanel = OverviewPanel;
window.PathPanel = PathPanel;
window.DistancePanel = DistancePanel;
window.CriticalPanel = CriticalPanel;
window.EmailInput = EmailInput;
