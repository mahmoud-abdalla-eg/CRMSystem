import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import {
  AddressBook,
  ChartBar,
  CheckCircle,
  GearSix,
  MagnifyingGlass,
  NotePencil,
  Plus,
  Pulse,
  Trash,
  UsersThree,
  X,
} from '@phosphor-icons/react';
import './styles.css';

const statuses = ['Lead', 'Prospect', 'Active', 'Inactive'];
const interactionTypes = ['Call', 'Email', 'Meeting', 'Note'];

const initialCustomers = [
  {
    id: 'c-1',
    name: 'Ahmed Hassan',
    email: 'ahmed@techcorp.com',
    phone: '+1 (312) 847-1928',
    company: 'TechCorp Inc.',
    status: 'Active',
    createdAt: '2026-06-01',
  },
  {
    id: 'c-2',
    name: 'Sarah Johnson',
    email: 'sarah@innovate.io',
    phone: '+1 (646) 405-7781',
    company: 'Innovate LLC',
    status: 'Prospect',
    createdAt: '2026-06-02',
  },
  {
    id: 'c-3',
    name: 'Mohamed Ali',
    email: 'mohamed@global.com',
    phone: '+1 (415) 672-0944',
    company: 'Global Systems',
    status: 'Lead',
    createdAt: '2026-06-03',
  },
];

const initialInteractions = [
  {
    id: 'i-1',
    customerId: 'c-2',
    type: 'Email',
    description: 'Sent product overview and scheduled a follow-up.',
    date: '2026-06-03 09:20',
  },
];

function App() {
  const [view, setView] = useState('Dashboard');
  const [customers, setCustomers] = useState(initialCustomers);
  const [interactions, setInteractions] = useState(initialInteractions);
  const [selectedId, setSelectedId] = useState(initialCustomers[0].id);
  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('All Statuses');
  const [typeFilter, setTypeFilter] = useState('All Types');
  const [customerFilter, setCustomerFilter] = useState('All Customers');
  const [modal, setModal] = useState(null);

  const selectedCustomer = customers.find((customer) => customer.id === selectedId) ?? null;

  const filteredCustomers = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    return customers.filter((customer) => {
      const matchesQuery =
        !normalized ||
        [customer.name, customer.email, customer.company].some((value) =>
          value.toLowerCase().includes(normalized),
        );
      const matchesStatus = statusFilter === 'All Statuses' || customer.status === statusFilter;
      return matchesQuery && matchesStatus;
    });
  }, [customers, query, statusFilter]);

  const filteredInteractions = useMemo(() => {
    return interactions.filter((interaction) => {
      const matchesType = typeFilter === 'All Types' || interaction.type === typeFilter;
      const matchesCustomer =
        customerFilter === 'All Customers' || interaction.customerId === customerFilter;
      return matchesType && matchesCustomer;
    });
  }, [interactions, typeFilter, customerFilter]);

  const stats = useMemo(() => {
    const active = customers.filter((customer) => customer.status === 'Active').length;
    const leads = customers.filter(
      (customer) => customer.status === 'Lead' || customer.status === 'Prospect',
    ).length;
    const inactive = customers.filter((customer) => customer.status === 'Inactive').length;
    return [
      { label: 'Total Customers', value: customers.length, tone: 'blue' },
      { label: 'Active Customers', value: active, tone: 'green' },
      { label: 'Leads & Prospects', value: leads, tone: 'amber' },
      { label: 'Inactive', value: inactive, tone: 'slate' },
    ];
  }, [customers]);

  const latestCustomer = customers[customers.length - 1];
  const latestInteraction = interactions[interactions.length - 1];

  function saveCustomer(form) {
    if (!form.name.trim() || !form.email.trim()) return false;

    if (form.id) {
      setCustomers((current) =>
        current.map((customer) => (customer.id === form.id ? { ...customer, ...form } : customer)),
      );
    } else {
      const customer = {
        ...form,
        id: `c-${crypto.randomUUID()}`,
        createdAt: new Date().toISOString().slice(0, 10),
      };
      setCustomers((current) => [...current, customer]);
      setSelectedId(customer.id);
    }

    setModal(null);
    return true;
  }

  function deleteCustomer(id) {
    setCustomers((current) => current.filter((customer) => customer.id !== id));
    setInteractions((current) => current.filter((interaction) => interaction.customerId !== id));
    setSelectedId((current) => {
      if (current !== id) return current;
      const next = customers.find((customer) => customer.id !== id);
      return next?.id ?? '';
    });
  }

  function saveInteraction(form) {
    if (!form.description.trim() || !selectedCustomer) return false;
    setInteractions((current) => [
      ...current,
      {
        id: `i-${crypto.randomUUID()}`,
        customerId: selectedCustomer.id,
        type: form.type,
        description: form.description.trim(),
        date: new Date().toISOString().slice(0, 16).replace('T', ' '),
      },
    ]);
    setModal(null);
    return true;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span>CRM</span>
          <small>Customer Manager</small>
        </div>
        <nav className="nav-list">
          {[
            ['Dashboard', ChartBar],
            ['Customers', UsersThree],
            ['Interactions', Pulse],
            ['Reports', AddressBook],
            ['Settings', GearSix],
          ].map(([label, Icon]) => (
            <button
              className={`nav-button ${view === label ? 'active' : ''}`}
              key={label}
              onClick={() => setView(label)}
              type="button"
            >
              <Icon size={18} weight="bold" />
              <span>{label}</span>
            </button>
          ))}
        </nav>
        <div className="quick-summary">
          <h2>Quick Summary</h2>
          <SummaryItem label="Customers" value={`${customers.length} total records`} />
          <SummaryItem label="Newest" value={latestCustomer?.name ?? 'No customers yet'} />
          <SummaryItem
            label="Last Activity"
            value={latestInteraction ? `${latestInteraction.type} logged` : 'No activity yet'}
          />
        </div>
      </aside>

      <main className="workspace">
        {view === 'Dashboard' && (
          <Dashboard stats={stats} customers={customers} interactions={interactions} />
        )}
        {view === 'Customers' && (
          <Customers
            customers={filteredCustomers}
            query={query}
            setQuery={setQuery}
            statusFilter={statusFilter}
            setStatusFilter={setStatusFilter}
            selectedCustomer={selectedCustomer}
            setSelectedId={setSelectedId}
            onAdd={() => setModal({ type: 'customer' })}
            onEdit={() => selectedCustomer && setModal({ type: 'customer', customer: selectedCustomer })}
            onDelete={() => selectedCustomer && deleteCustomer(selectedCustomer.id)}
            onLog={() => selectedCustomer && setModal({ type: 'interaction' })}
            interactions={interactions}
          />
        )}
        {view === 'Interactions' && (
          <Interactions
            customers={customers}
            interactions={filteredInteractions}
            typeFilter={typeFilter}
            setTypeFilter={setTypeFilter}
            customerFilter={customerFilter}
            setCustomerFilter={setCustomerFilter}
          />
        )}
        {view === 'Reports' && <Reports stats={stats} customers={customers} interactions={interactions} />}
        {view === 'Settings' && <Settings />}
      </main>

      {modal?.type === 'customer' && (
        <CustomerModal
          customer={modal.customer}
          onClose={() => setModal(null)}
          onSave={saveCustomer}
        />
      )}
      {modal?.type === 'interaction' && selectedCustomer && (
        <InteractionModal
          customer={selectedCustomer}
          onClose={() => setModal(null)}
          onSave={saveInteraction}
        />
      )}
    </div>
  );
}

function SummaryItem({ label, value }) {
  return (
    <div className="summary-item">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function Dashboard({ stats, customers, interactions }) {
  return (
    <section className="page">
      <Header title="Dashboard" subtitle="Today’s operating picture for customer work." />
      <div className="stat-grid compact">
        {stats.map((stat) => (
          <article className={`stat-tile ${stat.tone}`} key={stat.label}>
            <strong>{stat.value}</strong>
            <span>{stat.label}</span>
          </article>
        ))}
      </div>
      <div className="dashboard-grid">
        <Panel title="Recent Customers" className="wide-panel">
          <CustomerList customers={customers} />
        </Panel>
        <Panel title="Activity Feed">
          {interactions.length ? (
            <div className="activity-list">
              {interactions.slice(-5).reverse().map((item) => (
                <div className="activity-row" key={item.id}>
                  <CheckCircle size={18} weight="fill" />
                  <div>
                    <strong>{item.type}</strong>
                    <span>{item.description}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <EmptyState title="No activity logged" text="Log an interaction from a customer profile." />
          )}
        </Panel>
      </div>
    </section>
  );
}

function Customers(props) {
  const {
    customers,
    query,
    setQuery,
    statusFilter,
    setStatusFilter,
    selectedCustomer,
    setSelectedId,
    onAdd,
    onEdit,
    onDelete,
    onLog,
    interactions,
  } = props;

  const customerInteractions = selectedCustomer
    ? interactions.filter((interaction) => interaction.customerId === selectedCustomer.id)
    : [];

  return (
    <section className="page">
      <Header title="Customers" subtitle="Search, filter, and manage relationship records." />
      <div className="toolbar">
        <label className="search-box">
          <MagnifyingGlass size={18} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search name, email, company" />
        </label>
        <Select value={statusFilter} onChange={setStatusFilter} options={['All Statuses', ...statuses]} />
        <button className="primary-button" onClick={onAdd} type="button">
          <Plus size={18} weight="bold" />
          Add Customer
        </button>
      </div>
      <div className="customer-layout">
        <Panel className="table-panel">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Company</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((customer) => (
                <tr
                  className={selectedCustomer?.id === customer.id ? 'selected' : ''}
                  key={customer.id}
                  onClick={() => setSelectedId(customer.id)}
                >
                  <td>{customer.name}</td>
                  <td>{customer.email}</td>
                  <td>{customer.company}</td>
                  <td>
                    <Badge status={customer.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!customers.length && <EmptyState title="No customers found" text="Adjust search or add a new customer." />}
        </Panel>
        <Panel title="Customer Detail" className="detail-panel">
          {selectedCustomer ? (
            <div className="detail-stack">
              <div>
                <h3>{selectedCustomer.name}</h3>
                <p>{selectedCustomer.company}</p>
                <Badge status={selectedCustomer.status} />
              </div>
              <dl>
                <div><dt>Email</dt><dd>{selectedCustomer.email}</dd></div>
                <div><dt>Phone</dt><dd>{selectedCustomer.phone}</dd></div>
                <div><dt>Created</dt><dd>{selectedCustomer.createdAt}</dd></div>
              </dl>
              <div className="button-row">
                <button onClick={onEdit} type="button"><NotePencil size={16} />Edit</button>
                <button onClick={onLog} type="button"><Pulse size={16} />Log</button>
                <button className="danger" onClick={onDelete} type="button"><Trash size={16} />Delete</button>
              </div>
              <div className="mini-list">
                <h4>Recent Interactions</h4>
                {customerInteractions.length ? (
                  customerInteractions.slice(-3).reverse().map((item) => (
                    <div key={item.id}>
                      <strong>{item.type}</strong>
                      <span>{item.description}</span>
                    </div>
                  ))
                ) : (
                  <span className="muted">No interactions yet.</span>
                )}
              </div>
            </div>
          ) : (
            <EmptyState title="Select a customer" text="Choose a row to view details." />
          )}
        </Panel>
      </div>
    </section>
  );
}

function Interactions({ customers, interactions, typeFilter, setTypeFilter, customerFilter, setCustomerFilter }) {
  return (
    <section className="page">
      <Header title="Interactions" subtitle="Review customer communication history." />
      <div className="toolbar left">
        <Select value={typeFilter} onChange={setTypeFilter} options={['All Types', ...interactionTypes]} />
        <Select
          value={customerFilter}
          onChange={setCustomerFilter}
          options={[{ label: 'All Customers', value: 'All Customers' }, ...customers.map((c) => ({ label: c.name, value: c.id }))]}
        />
      </div>
      <Panel>
        {interactions.length ? (
          <div className="interaction-list">
            {interactions.map((interaction) => {
              const customer = customers.find((item) => item.id === interaction.customerId);
              return (
                <div className="interaction-card" key={interaction.id}>
                  <span>{interaction.type}</span>
                  <strong>{customer?.name ?? 'Unknown Customer'}</strong>
                  <p>{interaction.description}</p>
                  <time>{interaction.date}</time>
                </div>
              );
            })}
          </div>
        ) : (
          <EmptyState title="No interactions match" text="Change filters or log an activity from Customers." />
        )}
      </Panel>
    </section>
  );
}

function Reports({ stats, customers, interactions }) {
  const activeRate = customers.length
    ? Math.round((customers.filter((customer) => customer.status === 'Active').length / customers.length) * 100)
    : 0;
  return (
    <section className="page">
      <Header title="Reports" subtitle="A compact view of relationship quality and activity." />
      <div className="report-grid">
        <Panel title="Status Distribution">
          <div className="report-bars">
            {stats.slice(1).map((stat) => (
              <div key={stat.label}>
                <span>{stat.label}</span>
                <div><i style={{ width: `${Math.max(8, stat.value * 32)}px` }} /></div>
                <strong>{stat.value}</strong>
              </div>
            ))}
          </div>
        </Panel>
        <Panel title="Operational Notes">
          <ul className="report-notes">
            <li>{activeRate}% of customers are active.</li>
            <li>{interactions.length} interaction records are logged.</li>
            <li>{customers.length ? 'Customer data is ready for export or report submission.' : 'Add customers to begin reporting.'}</li>
          </ul>
        </Panel>
      </div>
    </section>
  );
}

function Settings() {
  return (
    <section className="page">
      <Header title="Settings" subtitle="Application information for the CRM web view." />
      <Panel>
        <div className="settings-list">
          <div><span>Application Name</span><strong>CRM System Web</strong></div>
          <div><span>Data Storage</span><strong>Browser State</strong></div>
          <div><span>Frontend</span><strong>Vite + React</strong></div>
        </div>
      </Panel>
      <Panel title="About">
        <p className="about-copy">
          This web view mirrors the Java Swing CRM project with customer management,
          interaction logging, dashboard summaries, reports, and cleaned form/dropdown behavior.
        </p>
      </Panel>
    </section>
  );
}

function CustomerList({ customers }) {
  if (!customers.length) return <EmptyState title="No customers yet" text="Add a customer to populate the dashboard." />;
  return (
    <div className="customer-list">
      {customers.map((customer) => (
        <div className="customer-list-row" key={customer.id}>
          <div>
            <strong>{customer.name}</strong>
            <span>{customer.email}</span>
          </div>
          <Badge status={customer.status} />
        </div>
      ))}
    </div>
  );
}

function Header({ title, subtitle }) {
  return (
    <header className="page-header">
      <div>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
    </header>
  );
}

function Panel({ title, children, className = '' }) {
  return (
    <section className={`panel ${className}`}>
      {title && <h2>{title}</h2>}
      {children}
    </section>
  );
}

function Select({ value, onChange, options }) {
  return (
    <select value={value} onChange={(event) => onChange(event.target.value)}>
      {options.map((option) => {
        const valueText = typeof option === 'string' ? option : option.value;
        const labelText = typeof option === 'string' ? option : option.label;
        return (
          <option value={valueText} key={valueText}>
            {labelText}
          </option>
        );
      })}
    </select>
  );
}

function Badge({ status }) {
  return <span className={`badge ${status.toLowerCase()}`}>{status}</span>;
}

function EmptyState({ title, text }) {
  return (
    <div className="empty-state">
      <AddressBook size={28} />
      <strong>{title}</strong>
      <span>{text}</span>
    </div>
  );
}

function CustomerModal({ customer, onClose, onSave }) {
  const [form, setForm] = useState(
    customer ?? {
      name: '',
      email: '',
      phone: '',
      company: '',
      status: 'Lead',
    },
  );
  const [error, setError] = useState('');

  function submit(event) {
    event.preventDefault();
    if (!form.name.trim() || !form.email.trim()) {
      setError('Name and email are required.');
      return;
    }
    onSave(form);
  }

  return (
    <Modal title={customer ? 'Edit Customer' : 'New Customer'} onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <Field label="Name" value={form.name} onChange={(name) => setForm({ ...form, name })} />
        <Field label="Email" value={form.email} onChange={(email) => setForm({ ...form, email })} />
        <Field label="Phone" value={form.phone} onChange={(phone) => setForm({ ...form, phone })} />
        <Field label="Company" value={form.company} onChange={(company) => setForm({ ...form, company })} />
        <label className="field">
          <span>Status</span>
          <Select value={form.status} onChange={(status) => setForm({ ...form, status })} options={statuses} />
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="modal-actions">
          <button type="button" onClick={onClose}>Cancel</button>
          <button className="primary-button" type="submit">Save Customer</button>
        </div>
      </form>
    </Modal>
  );
}

function InteractionModal({ customer, onClose, onSave }) {
  const [form, setForm] = useState({ type: 'Call', description: '' });
  const [error, setError] = useState('');

  function submit(event) {
    event.preventDefault();
    if (!form.description.trim()) {
      setError('Description is required.');
      return;
    }
    onSave(form);
  }

  return (
    <Modal title={`Log Interaction: ${customer.name}`} onClose={onClose}>
      <form className="modal-form" onSubmit={submit}>
        <label className="field">
          <span>Type</span>
          <Select value={form.type} onChange={(type) => setForm({ ...form, type })} options={interactionTypes} />
        </label>
        <label className="field">
          <span>Description</span>
          <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="modal-actions">
          <button type="button" onClick={onClose}>Cancel</button>
          <button className="primary-button" type="submit">Save</button>
        </div>
      </form>
    </Modal>
  );
}

function Field({ label, value, onChange }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function Modal({ title, children, onClose }) {
  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-title">
          <h2>{title}</h2>
          <button type="button" onClick={onClose} aria-label="Close dialog">
            <X size={18} weight="bold" />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

createRoot(document.getElementById('root')).render(<App />);
