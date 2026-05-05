// graph.js — JS port of the GrafoContatos engine.
// Mirrors the Java spec: directed, weighted, labeled graph with DFS, BFS,
// distance-D, and inverse-weight Dijkstra ("caminho crítico").

(function () {
  /** Builds an adjacency-map graph from edge tuples [fromIdx, toIdx, weight]. */
  function buildGraph(people, edges) {
    const verts = people.map(p => ({ email: p.e, label: p.d, in: [], out: [] }));
    const byEmail = {};
    verts.forEach(v => (byEmail[v.email] = v));

    const adj = new Map();   // origem -> Map(destino -> aresta)
    verts.forEach(v => adj.set(v.email, new Map()));

    for (const [a, b, w] of edges) {
      const o = verts[a], d = verts[b];
      if (!o || !d) continue;
      const m = adj.get(o.email);
      if (m.has(d.email)) {
        m.get(d.email).peso += w; // matches incrementarPeso semantics
      } else {
        m.set(d.email, { origem: o.email, destino: d.email, peso: w });
      }
    }

    // Recompute in/out degree neighbour lists
    verts.forEach(v => { v.in = []; v.out = []; });
    for (const [origem, m] of adj.entries()) {
      for (const [destino, aresta] of m.entries()) {
        byEmail[origem].out.push(aresta);
        byEmail[destino].in.push(aresta);
      }
    }

    let edgeCount = 0;
    for (const m of adj.values()) edgeCount += m.size;

    return {
      vertices: verts,
      byEmail,
      adj,
      numVertices: verts.length,
      numArestas: edgeCount,
    };
  }

  /** Top-K out-degree (count of distinct destinations, not summed weight). */
  function topGrauSaida(g, k = 20) {
    return g.vertices
      .map(v => ({ email: v.email, label: v.label, grau: v.out.length }))
      .sort((a, b) => b.grau - a.grau || a.email.localeCompare(b.email))
      .slice(0, k);
  }

  /** Top-K in-degree. */
  function topGrauEntrada(g, k = 20) {
    return g.vertices
      .map(v => ({ email: v.email, label: v.label, grau: v.in.length }))
      .sort((a, b) => b.grau - a.grau || a.email.localeCompare(b.email))
      .slice(0, k);
  }

  /** Iterative DFS with predecessor map; cycle-safe. Returns path or []. */
  function dfs(g, src, dst) {
    if (!g.byEmail[src] || !g.byEmail[dst]) return { path: [], visited: 0 };
    if (src === dst) return { path: [src], visited: 1 };
    const stack = [src];
    const visited = new Set([src]);
    const pred = new Map();
    while (stack.length) {
      const cur = stack.pop();
      if (cur === dst) return reconstruct(pred, src, dst, visited.size);
      const m = g.adj.get(cur);
      if (!m) continue;
      // iterate adjacencies in deterministic order (alphabetical)
      const keys = [...m.keys()].sort();
      for (const nxt of keys) {
        if (!visited.has(nxt)) {
          visited.add(nxt);
          pred.set(nxt, cur);
          if (nxt === dst) return reconstruct(pred, src, dst, visited.size);
          stack.push(nxt);
        }
      }
    }
    return { path: [], visited: visited.size };
  }

  /** BFS with predecessor map; returns shortest-by-edges path or []. */
  function bfs(g, src, dst) {
    if (!g.byEmail[src] || !g.byEmail[dst]) return { path: [], visited: 0 };
    if (src === dst) return { path: [src], visited: 1 };
    const queue = [src];
    const visited = new Set([src]);
    const pred = new Map();
    while (queue.length) {
      const cur = queue.shift();
      const m = g.adj.get(cur);
      if (!m) continue;
      for (const nxt of m.keys()) {
        if (!visited.has(nxt)) {
          visited.add(nxt);
          pred.set(nxt, cur);
          if (nxt === dst) return reconstruct(pred, src, dst, visited.size);
          queue.push(nxt);
        }
      }
    }
    return { path: [], visited: visited.size };
  }

  function reconstruct(pred, src, dst, visited) {
    const path = [dst];
    let cur = dst;
    while (cur !== src) {
      cur = pred.get(cur);
      if (cur == null) return { path: [], visited };
      path.push(cur);
    }
    return { path: path.reverse(), visited };
  }

  /** All vertices at exactly distance D (edge count) from src. */
  function distanciaD(g, src, D) {
    if (!g.byEmail[src]) return [];
    if (D === 0) return [src];
    const dist = new Map([[src, 0]]);
    const queue = [src];
    const result = [];
    while (queue.length) {
      const cur = queue.shift();
      const d = dist.get(cur);
      if (d === D) continue;
      const m = g.adj.get(cur);
      if (!m) continue;
      for (const nxt of m.keys()) {
        if (!dist.has(nxt)) {
          dist.set(nxt, d + 1);
          if (d + 1 === D) result.push(nxt);
          else queue.push(nxt);
        }
      }
    }
    return result.sort();
  }

  /** Inverse-weight Dijkstra. Returns path + per-edge weights + dependencia. */
  function caminhoCritico(g, src, dst) {
    if (!g.byEmail[src] || !g.byEmail[dst]) return null;
    const dist = new Map();
    const pred = new Map();
    g.vertices.forEach(v => dist.set(v.email, Infinity));
    dist.set(src, 0);
    // tiny priority queue (linear extract — fine for 50-node demo)
    const open = new Set(g.vertices.map(v => v.email));
    while (open.size) {
      let u = null, best = Infinity;
      for (const x of open) {
        const dx = dist.get(x);
        if (dx < best) { best = dx; u = x; }
      }
      if (u == null || best === Infinity) break;
      open.delete(u);
      if (u === dst) break;
      const m = g.adj.get(u);
      if (!m) continue;
      for (const [v, aresta] of m.entries()) {
        if (!open.has(v)) continue;
        const cost = 1.0 / aresta.peso;
        const alt = dist.get(u) + cost;
        if (alt < dist.get(v)) {
          dist.set(v, alt);
          pred.set(v, u);
        }
      }
    }
    if (dist.get(dst) === Infinity) {
      return { path: [], edges: [], custoInverso: 0, dependencia: 0 };
    }
    const path = [dst];
    let cur = dst;
    while (cur !== src) {
      cur = pred.get(cur);
      if (cur == null) return { path: [], edges: [], custoInverso: 0, dependencia: 0 };
      path.push(cur);
    }
    path.reverse();
    const edges = [];
    let dependencia = 0;
    for (let i = 0; i < path.length - 1; i++) {
      const ar = g.adj.get(path[i]).get(path[i + 1]);
      edges.push(ar);
      dependencia += ar.peso;
    }
    return { path, edges, custoInverso: dist.get(dst), dependencia };
  }

  /** Edge between two vertices, or null. */
  function arestaEntre(g, a, b) {
    const m = g.adj.get(a);
    return m ? (m.get(b) || null) : null;
  }

  window.GrafoContatos = {
    buildGraph, topGrauSaida, topGrauEntrada, dfs, bfs,
    distanciaD, caminhoCritico, arestaEntre,
  };
})();
