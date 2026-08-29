import { useEffect, useState } from 'react'

type Status = 'checking…' | 'connected' | 'unreachable'

function App() {
  const [status, setStatus] = useState<Status>('checking…')

  useEffect(() => {
    fetch('/api/health-check')
        .then((res) => setStatus(res.ok ? 'connected' : 'unreachable'))
        .catch(() => setStatus('unreachable'))
  }, [])

  return (
      <main>
        <h1>PlanTogether</h1>
        <p>{`API: ${status}`}</p>
      </main>
  )
}

export default App