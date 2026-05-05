// pathviz.jsx — Small node-link visualization for path results.
// Lays out path nodes on a horizontal serpentine; highlights the path edges
// against a faded background of nearby graph context.

const { useMemo } = React;

function PathViz({ graph, path, edges, height = 240, channelHue = 75 }) {
  const layout = useMemo(() => {
    if (!path || path.length === 0) return null;
    const W = 800, H = height;
    const cols = Math.min(path.length, 6);
    const rows = Math.ceil(path.length / cols);
    const padX = 60, padY = 40;
    const stepX = (W - padX * 2) / Math.max(cols - 1, 1);
    const stepY = rows > 1 ? (H - padY * 2) / (rows - 1) : 0;
    const pts = path.map((email, i) => {
      const r = Math.floor(i / cols);
      const inRow = i % cols;
      const x = padX + (r % 2 === 0 ? inRow : (cols - 1 - inRow)) * stepX;
      const y = padY + r * stepY;
      return { email, x, y, label: graph.byEmail[email]?.label || "" };
    });
    return { pts, W, H };
  }, [graph, path, height]);

  if (!layout) return null;
  const { pts, W, H } = layout;

  // contextual neighbors of path nodes (greyed)
  const ctx = [];
  const pathSet = new Set(path);
  for (const p of pts) {
    const m = graph.adj.get(p.email);
    if (!m) continue;
    let i = 0;
    for (const [nb] of m.entries()) {
      if (pathSet.has(nb)) continue;
      if (i++ > 3) break;
      ctx.push({ from: p, toEmail: nb });
    }
  }

  const accent = `oklch(0.82 0.15 ${channelHue})`;
  const accentSoft = `oklch(0.82 0.15 ${channelHue} / 0.30)`;

  return (
    <div className="path-viz">
      <div className="pv-h">
        <span>topologia · projeção do caminho</span>
        <span>{pts.length} nós · {edges?.length ?? Math.max(pts.length - 1, 0)} arestas</span>
      </div>
      <svg viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid meet">
        <defs>
          <marker id="arr" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
            <path d="M 0 0 L 10 5 L 0 10 z" fill={accent} />
          </marker>
          <marker id="arr-soft" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse">
            <path d="M 0 0 L 10 5 L 0 10 z" fill="var(--rule)" />
          </marker>
        </defs>

        {/* Context edges (faint) */}
        {ctx.map((c, i) => (
          <g key={`c${i}`} opacity="0.55">
            <line
              x1={c.from.x} y1={c.from.y}
              x2={c.from.x + 30 * Math.cos(i * 1.7)}
              y2={c.from.y + 30 * Math.sin(i * 1.7)}
              stroke="var(--rule)" strokeWidth="0.5"
            />
            <circle
              cx={c.from.x + 30 * Math.cos(i * 1.7)}
              cy={c.from.y + 30 * Math.sin(i * 1.7)}
              r="2.5" fill="var(--faint)"
            />
          </g>
        ))}

        {/* Path edges */}
        {pts.slice(0, -1).map((p, i) => {
          const q = pts[i + 1];
          const w = edges?.[i]?.peso;
          const mx = (p.x + q.x) / 2;
          const my = (p.y + q.y) / 2;
          return (
            <g key={`e${i}`}>
              <line
                x1={p.x} y1={p.y} x2={q.x} y2={q.y}
                stroke={accent} strokeWidth="1.5"
                markerEnd="url(#arr)"
              />
              {w != null && (
                <g transform={`translate(${mx} ${my})`}>
                  <rect x="-14" y="-8" width="28" height="16" fill="var(--bg)" stroke={accentSoft} strokeWidth="0.5" />
                  <text x="0" y="3" textAnchor="middle"
                        fontFamily="var(--ff-mono)" fontSize="10" fill={accent}
                        style={{ fontVariantNumeric: "tabular-nums" }}>
                    {w}
                  </text>
                </g>
              )}
            </g>
          );
        })}

        {/* Path nodes */}
        {pts.map((p, i) => {
          const isStart = i === 0;
          const isEnd = i === pts.length - 1;
          const r = isStart || isEnd ? 6 : 4;
          return (
            <g key={`n${i}`}>
              {(isStart || isEnd) && (
                <circle cx={p.x} cy={p.y} r={r + 6} fill="none" stroke={accent} strokeWidth="0.5" opacity="0.6" />
              )}
              <circle cx={p.x} cy={p.y} r={r} fill={isStart ? "var(--ink)" : accent} />
              <text x={p.x} y={p.y - 14}
                    textAnchor="middle"
                    fontFamily="var(--ff-mono)" fontSize="9.5"
                    fill="var(--ink-2)"
                    style={{ letterSpacing: "0.02em" }}>
                {p.email.split("@")[0]}
              </text>
              <text x={p.x} y={p.y + 18}
                    textAnchor="middle"
                    fontFamily="var(--ff-mono)" fontSize="8.5"
                    fill="var(--faint)"
                    style={{ letterSpacing: "0.04em", textTransform: "uppercase" }}>
                {isStart ? "origem" : isEnd ? "destino" : `nó ${i}`}
              </text>
            </g>
          );
        })}
      </svg>
    </div>
  );
}

window.PathViz = PathViz;
