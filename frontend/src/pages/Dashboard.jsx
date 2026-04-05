import { useEffect, useState } from 'react'
import { getDashboard, getProducts, getAlerts } from '../api/inventory'
import { TrendingDown, AlertTriangle, Package, DollarSign, RefreshCw } from 'lucide-react'

function StockBar({ qty, threshold, max }) {
  const pct = Math.min((qty / (max || 1)) * 100, 100)
  const color = qty === 0 ? 'var(--red)' : qty <= threshold ? 'var(--amber)' : 'var(--green)'
  return (
    <div className="stock-bar-wrap">
      <div className="stock-bar-bg">
        <div className="stock-bar-fill" style={{ width: `${pct}%`, background: color }} />
      </div>
      <span className="mono" style={{ color, minWidth: 32, textAlign: 'right' }}>{qty}</span>
    </div>
  )
}

function StockBadge({ qty, threshold }) {
  if (qty === 0)        return <span className="badge badge-red">Out of stock</span>
  if (qty <= threshold) return <span className="badge badge-amber">Low stock</span>
  return                       <span className="badge badge-green">In stock</span>
}

export default function Dashboard() {
  const [summary,  setSummary]  = useState(null)
  const [products, setProducts] = useState([])
  const [alerts,   setAlerts]   = useState({})
  const [loading,  setLoading]  = useState(true)

  const refresh = () => {
    Promise.all([getDashboard(), getProducts(), getAlerts()])
      .then(([s, p, a]) => { setSummary(s); setProducts(p); setAlerts(a); setLoading(false) })
      .catch(() => setLoading(false))
  }

  useEffect(() => { refresh() }, [])

  const maxQty = products.length ? Math.max(...products.map(p => p.quantity)) : 1

  if (loading) return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
      <RefreshCw size={20} color="var(--text-muted)" />
    </div>
  )

  const criticalAlerts = [
    ...(alerts.velocity || []).filter(a => a.severity === 'CRITICAL'),
    ...(alerts.expiry   || []).filter(a => a.severity === 'CRITICAL'),
    ...(alerts.lowStock || []).filter(a => a.severity === 'CRITICAL'),
  ].slice(0, 3)

  return (
    <div className="page-animate">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div className="page-title">Dashboard</div>
          <div className="page-subtitle">Live inventory overview</div>
        </div>
        <button className="btn" onClick={refresh}><RefreshCw size={13} /> Refresh</button>
      </div>

      <div className="stat-grid">
        {[
          { label: 'Total Products', value: summary?.totalProducts ?? '-', sub: 'in HashMap',      color: 'var(--blue)',  icon: Package       },
          { label: 'Total Alerts',   value: summary?.totalAlerts   ?? '-', sub: '3 alert types',  color: 'var(--red)',   icon: AlertTriangle },
          { label: 'Low Stock',      value: summary?.lowStockCount ?? '-', sub: 'MinHeap alerts', color: 'var(--amber)', icon: TrendingDown  },
          { label: 'Inventory Value',value: `₹${(summary?.totalValue ?? 0).toLocaleString('en-IN')}`, sub: 'total value', color: 'var(--green)', icon: DollarSign },
        ].map(({ label, value, sub, color, icon: Icon }) => (
          <div className="stat-card" key={label}>
            <div className="stat-accent" style={{ background: color }} />
            <div className="stat-label">{label}</div>
            <div className="stat-value" style={{ color }}>{value}</div>
            <div className="stat-sub">{sub}</div>
          </div>
        ))}
      </div>

      <div className="card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <div className="section-label" style={{ marginBottom: 0 }}>All products</div>
          <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>{products.length} items</span>
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th><th>Product</th><th>Category</th>
                <th>Stock</th><th>Price</th><th>Expiry</th><th>Status</th>
              </tr>
            </thead>
            <tbody>
              {products.sort((a, b) => a.quantity - b.quantity).map(p => {
                const daysLeft = Math.ceil((new Date(p.expiryDate) - Date.now()) / 86400000)
                return (
                  <tr key={p.id}>
                    <td><span className="mono" style={{ color: 'var(--text-muted)' }}>{p.id}</span></td>
                    <td style={{ fontWeight: 500 }}>{p.name}</td>
                    <td><span className="badge badge-gray">{p.category}</span></td>
                    <td style={{ minWidth: 160 }}><StockBar qty={p.quantity} threshold={p.threshold} max={maxQty} /></td>
                    <td className="mono">₹{p.price}</td>
                    <td style={{ color: daysLeft <= 3 ? 'var(--red)' : daysLeft <= 7 ? 'var(--amber)' : 'var(--text-secondary)', fontSize: 12 }}>
                      {p.expiryDate} {daysLeft <= 7 && <span style={{ fontSize: 10 }}>({daysLeft}d)</span>}
                    </td>
                    <td><StockBadge qty={p.quantity} threshold={p.threshold} /></td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>

      {criticalAlerts.length > 0 && (
        <div className="card" style={{ borderColor: 'rgba(244,63,94,0.2)' }}>
          <div className="section-label" style={{ color: 'var(--red)', marginBottom: 12 }}>Critical alerts</div>
          {criticalAlerts.map((a, i) => (
            <div key={i} className="alert-item" style={{ borderColor: 'rgba(244,63,94,0.15)', background: 'var(--red-bg)' }}>
              <div className="alert-dot pulse" style={{ background: 'var(--red)' }} />
              <div>
                <div style={{ fontWeight: 500, fontSize: 13 }}>{a.productName}</div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{a.message}</div>
              </div>
              <span className="badge badge-red" style={{ marginLeft: 'auto' }}>
                {a.type === 'VELOCITY' ? `~${a.daysToStockout}d left` : a.type}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}