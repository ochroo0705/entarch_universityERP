import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  cancelFeeInvoice,
  createFeeInvoice,
  createFeeItem,
  createMealItem,
  createMealPlan,
  getFeeInvoices,
  getFeeItems,
  getAllUsers,
  getMealItems,
  getMealPlans,
  getMealPurchaseDailySummary,
  getMealPurchases,
  getStudentFinanceSummary,
  recordFeePayment,
  recordMealPurchase,
  updateFeeInvoice,
  waiveFeeInvoice,
} from '../../api/endpoints';
import { useAuth } from '../../context/AuthContext';
import ChildSelector, { useChild } from '../../components/ChildSelector';
import SectionCard from '../../components/ui/SectionCard';
import SearchableSelect from '../../components/ui/SearchableSelect';
import { EmptyState, ErrorState, LoadingState } from '../../components/ui/StateBlock';

const money = (value) => new Intl.NumberFormat(undefined, { style: 'currency', currency: 'MNT', maximumFractionDigits: 0 }).format(Number(value || 0));
const createEmptyInvoiceLine = () => ({ feeItemId: '', description: '', amount: '' });
const INVOICE_STATUSES = ['DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'WAIVED', 'OVERDUE', 'CANCELLED'];
const UNIVERSITY_DEMO_CAFETERIA_ENABLED = false;
const normalizeRole = (value) => String(value || '').replace(/^ROLE_/, '');
const userHasRole = (user, role) => {
  const roles = Array.isArray(user?.roles) && user.roles.length > 0 ? user.roles : [user?.role];
  return roles.map(normalizeRole).includes(role);
};
const escapeHtml = (value) => String(value ?? '').replace(/[&<>"']/g, (character) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#039;',
}[character]));

function printPaymentReceipt({ invoice, payment, t }) {
  const receiptWindow = window.open('', '_blank', 'width=760,height=900');
  if (!receiptWindow) return;

  const rows = [
    [t('finance.receipt.invoice'), invoice.invoiceNumber],
    [t('finance.receipt.student'), invoice.studentName],
    [t('finance.receipt.paymentDate'), payment.paymentDate || '-'],
    [t('finance.receipt.amount'), money(payment.amount)],
    [t('finance.receipt.method'), t(`finance.method.${payment.method}`, { defaultValue: payment.method })],
    [t('finance.receipt.status'), payment.status],
    [t('finance.receipt.reference'), payment.referenceNumber || '-'],
  ];

  receiptWindow.document.open();
  receiptWindow.document.write(`
    <!doctype html>
    <html>
      <head>
        <title>${escapeHtml(t('finance.receipt.title'))}</title>
        <style>
          body { color: #111827; font-family: Arial, sans-serif; margin: 40px; }
          main { border: 1px solid #d1d5db; border-radius: 8px; max-width: 680px; padding: 28px; }
          h1 { font-size: 24px; margin: 0 0 6px; }
          p { color: #6b7280; margin: 0 0 24px; }
          table { border-collapse: collapse; width: 100%; }
          th, td { border-bottom: 1px solid #e5e7eb; padding: 12px 0; text-align: left; }
          th { color: #6b7280; font-weight: 600; width: 38%; }
          .total { font-size: 20px; font-weight: 700; }
          @media print { body { margin: 24px; } main { border-color: #111827; } }
        </style>
      </head>
      <body>
        <main>
          <h1>${escapeHtml(t('finance.receipt.title'))}</h1>
          <p>${escapeHtml(t('finance.receipt.subtitle'))}</p>
          <table>
            <tbody>
              ${rows.map(([label, value]) => `
                <tr>
                  <th>${escapeHtml(label)}</th>
                  <td class="${label === t('finance.receipt.amount') ? 'total' : ''}">${escapeHtml(value)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </main>
        <script>
          window.addEventListener('load', () => {
            window.print();
          });
        </script>
      </body>
    </html>
  `);
  receiptWindow.document.close();
}

const buildUserOption = (user) => ({
  value: user.id,
  label: [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || user.email || `#${user.id}`,
  meta: [user.username ? `@${user.username}` : null, user.email || null].filter(Boolean).join(' - ') || null,
});

function Stat({ label, value }) {
  return (
    <div className="finance-stat">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function TextInput({ label, value, onChange, type = 'text', placeholder }) {
  return (
    <label className="form-group">
      <span>{label}</span>
      <input className="form-control" type={type} value={value} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function SelectInput({ label, value, onChange, options }) {
  return (
    <label className="form-group">
      <span>{label}</span>
      <select className="form-control" value={value} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
      </select>
    </label>
  );
}

function StudentLookup({ label, value, onChange, disabled = false, selectedFallback = null }) {
  const { t } = useTranslation();
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [selectedOption, setSelectedOption] = useState(null);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setDebouncedSearch(searchInput.trim()), 300);
    return () => window.clearTimeout(timeoutId);
  }, [searchInput]);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    getAllUsers({
      page: 1,
      pageSize: 25,
      role: 1,
      sortBy: 'name',
      sortOrder: 'asc',
      search: debouncedSearch || undefined,
    })
      .then((response) => {
        if (ignore) return;
        setStudents(Array.isArray(response.data?.items) ? response.data.items : []);
      })
      .catch((err) => {
        if (ignore) return;
        console.error('Failed to load students for finance form', err);
        setStudents([]);
      })
      .finally(() => {
        if (ignore) return;
        setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedSearch]);

  const studentOptions = useMemo(() => students.map(buildUserOption), [students]);
  const availableOptions = useMemo(() => {
    const fallback = selectedOption || selectedFallback;
    if (!fallback) return studentOptions;
    return studentOptions.some((option) => String(option.value) === String(fallback.value))
      ? studentOptions
      : [fallback, ...studentOptions];
  }, [selectedFallback, selectedOption, studentOptions]);

  useEffect(() => {
    if (!value) {
      setSelectedOption(null);
      return;
    }

    const matchingStudent = availableOptions.find((option) => String(option.value) === String(value));
    if (matchingStudent) {
      setSelectedOption(matchingStudent);
    }
  }, [availableOptions, value]);

  return (
    <div className="form-group">
      <label>{label}</label>
      <SearchableSelect
        options={availableOptions}
        value={value}
        onChange={(nextValue, option) => {
          onChange(String(nextValue || ''));
          setSelectedOption(option || null);
        }}
        searchValue={searchInput}
        onSearchChange={setSearchInput}
        placeholder={t('finance.forms.selectStudent')}
        searchPlaceholder={t('common.search')}
        emptyLabel={t('finance.forms.noStudentsFound')}
        loadingLabel={t('finance.forms.loadingStudents')}
        isLoading={loading}
        disabled={disabled}
      />
    </div>
  );
}

function AdminFinanceForms({ feeItems, mealItems, mealPlans, invoices, editingInvoice, canManageFinance, canManageCafeteria, onCancelEdit, onSaved }) {
  const { t } = useTranslation();
  const [feeItem, setFeeItem] = useState({ name: '', description: '', category: 'TUITION', amount: '' });
  const [invoice, setInvoice] = useState({ studentId: '', dueDate: '', notes: '', lines: [createEmptyInvoiceLine()] });
  const [payment, setPayment] = useState({ invoiceId: '', amount: '', method: 'CASH', referenceNumber: '' });
  const [mealPlan, setMealPlan] = useState({ name: '', description: '', pricePerMeal: '' });
  const [mealItem, setMealItem] = useState({ name: '', description: '', mealType: 'LUNCH', price: '' });
  const [purchase, setPurchase] = useState({ studentId: '', mealItemId: '', mealPlanId: '', quantity: '1' });
  const [saving, setSaving] = useState('');
  const invoiceTotal = useMemo(
    () => invoice.lines.reduce((total, line) => total + Number(line.amount || 0), 0),
    [invoice.lines]
  );
  const invoiceLines = useMemo(
    () => invoice.lines
      .filter((line) => line.feeItemId || line.description.trim() || Number(line.amount || 0) > 0)
      .map((line) => ({
        feeItemId: line.feeItemId ? Number(line.feeItemId) : null,
        description: line.description,
        amount: Number(line.amount || 0),
      })),
    [invoice.lines]
  );
  const editingStudentOption = useMemo(() => (
    editingInvoice
      ? { value: editingInvoice.studentId, label: editingInvoice.studentName || `#${editingInvoice.studentId}`, meta: editingInvoice.invoiceNumber }
      : null
  ), [editingInvoice]);

  useEffect(() => {
    if (!editingInvoice) return;
    setInvoice({
      studentId: String(editingInvoice.studentId || ''),
      dueDate: editingInvoice.dueDate || '',
      notes: editingInvoice.notes || '',
      lines: editingInvoice.lines?.length
        ? editingInvoice.lines.map((line) => ({
          feeItemId: line.feeItemId ? String(line.feeItemId) : '',
          description: line.description || '',
          amount: line.amount ?? '',
        }))
        : [createEmptyInvoiceLine()],
    });
  }, [editingInvoice]);

  const submit = async (key, action) => {
    setSaving(key);
    try {
      await action();
      await onSaved();
    } finally {
      setSaving('');
    }
  };

  const resetInvoiceForm = () => {
    setInvoice({ studentId: '', dueDate: '', notes: '', lines: [createEmptyInvoiceLine()] });
    onCancelEdit?.();
  };

  const submitInvoice = () => submit('invoice', async () => {
    const payload = {
      studentId: Number(invoice.studentId),
      dueDate: invoice.dueDate || null,
      notes: invoice.notes,
      lines: invoiceLines,
    };
    if (editingInvoice) {
      await updateFeeInvoice(editingInvoice.id, payload);
      resetInvoiceForm();
      return;
    }
    await createFeeInvoice(payload);
  });

  return (
    <div className="finance-admin-grid">
      {canManageFinance ? <SectionCard title={t('finance.forms.feeItemTitle')} subtitle={t('finance.forms.feeItemSubtitle')}>
        <div className="form-grid">
          <TextInput label={t('finance.fields.name')} value={feeItem.name} onChange={(name) => setFeeItem({ ...feeItem, name })} />
          <SelectInput label={t('finance.fields.category')} value={feeItem.category} onChange={(category) => setFeeItem({ ...feeItem, category })} options={['TUITION', 'ACTIVITY', 'TRANSPORT', 'CAFETERIA', 'OTHER'].map((value) => ({ value, label: t(`finance.category.${value}`) }))} />
          <TextInput label={t('finance.fields.amount')} type="number" value={feeItem.amount} onChange={(amount) => setFeeItem({ ...feeItem, amount })} />
          <TextInput label={t('finance.fields.description')} value={feeItem.description} onChange={(description) => setFeeItem({ ...feeItem, description })} />
        </div>
        <button className="btn btn-primary" disabled={saving === 'feeItem'} onClick={() => submit('feeItem', () => createFeeItem({ ...feeItem, amount: Number(feeItem.amount || 0), active: true }))}>{saving === 'feeItem' ? t('common.saving') : t('finance.actions.createFeeItem')}</button>
      </SectionCard> : null}

      {canManageFinance ? <SectionCard title={editingInvoice ? t('finance.forms.editInvoiceTitle') : t('finance.forms.invoiceTitle')} subtitle={editingInvoice ? t('finance.forms.editInvoiceSubtitle') : t('finance.forms.invoiceSubtitle')}>
        <div className="form-grid">
          <StudentLookup label={t('finance.fields.student')} value={invoice.studentId} onChange={(studentId) => setInvoice({ ...invoice, studentId })} disabled={saving === 'invoice'} selectedFallback={editingStudentOption} />
          <TextInput label={t('finance.fields.dueDate')} type="date" value={invoice.dueDate} onChange={(dueDate) => setInvoice({ ...invoice, dueDate })} />
          <TextInput label={t('finance.fields.notes')} value={invoice.notes} onChange={(notes) => setInvoice({ ...invoice, notes })} />
        </div>
        <div className="invoice-line-list">
          {invoice.lines.map((line, index) => (
            <div className="invoice-line" key={index}>
              <div className="invoice-line-header">
                <strong>{t('finance.forms.invoiceLineLabel', { number: index + 1 })}</strong>
                {invoice.lines.length > 1 ? (
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => setInvoice((current) => ({ ...current, lines: current.lines.filter((_, lineIndex) => lineIndex !== index) }))}
                  >
                    {t('finance.actions.removeLine')}
                  </button>
                ) : null}
              </div>
              <div className="form-grid">
                <SelectInput
                  label={t('finance.fields.feeItem')}
                  value={line.feeItemId}
                  onChange={(feeItemId) => {
                    const selected = feeItems.find((item) => String(item.id) === String(feeItemId));
                    setInvoice((current) => ({
                      ...current,
                      lines: current.lines.map((currentLine, lineIndex) => (
                        lineIndex === index
                          ? { ...currentLine, feeItemId, description: selected?.name || currentLine.description, amount: selected?.amount || currentLine.amount }
                          : currentLine
                      )),
                    }));
                  }}
                  options={[{ value: '', label: t('finance.forms.customLine') }, ...feeItems.map((item) => ({ value: String(item.id), label: `${item.name} - ${money(item.amount)}` }))]}
                />
                <TextInput label={t('finance.fields.description')} value={line.description} onChange={(description) => setInvoice((current) => ({ ...current, lines: current.lines.map((currentLine, lineIndex) => (lineIndex === index ? { ...currentLine, description } : currentLine)) }))} />
                <TextInput label={t('finance.fields.amount')} type="number" value={line.amount} onChange={(amount) => setInvoice((current) => ({ ...current, lines: current.lines.map((currentLine, lineIndex) => (lineIndex === index ? { ...currentLine, amount } : currentLine)) }))} />
              </div>
            </div>
          ))}
        </div>
        <div className="invoice-line-actions">
          <button type="button" className="btn btn-secondary" onClick={() => setInvoice((current) => ({ ...current, lines: [...current.lines, createEmptyInvoiceLine()] }))}>{t('finance.actions.addLine')}</button>
          <div className="invoice-total"><span>{t('finance.fields.invoiceTotal')}</span><strong>{money(invoiceTotal)}</strong></div>
        </div>
        <div className="button-row">
          <button className="btn btn-primary" disabled={saving === 'invoice' || !invoice.studentId || invoiceLines.length === 0} onClick={submitInvoice}>{saving === 'invoice' ? t('common.saving') : (editingInvoice ? t('finance.actions.updateInvoice') : t('finance.actions.createInvoice'))}</button>
          {editingInvoice ? <button type="button" className="btn btn-secondary" onClick={resetInvoiceForm}>{t('common.cancel')}</button> : null}
        </div>
      </SectionCard> : null}

      {canManageFinance ? <SectionCard title={t('finance.forms.paymentTitle')} subtitle={t('finance.forms.paymentSubtitle')}>
        <div className="form-grid">
          <SelectInput label={t('finance.fields.invoice')} value={payment.invoiceId} onChange={(invoiceId) => setPayment({ ...payment, invoiceId })} options={[{ value: '', label: t('finance.forms.selectInvoice') }, ...invoices.map((item) => ({ value: String(item.id), label: `${item.invoiceNumber} - ${item.studentName} - ${money(item.balance)}` }))]} />
          <TextInput label={t('finance.fields.amount')} type="number" value={payment.amount} onChange={(amount) => setPayment({ ...payment, amount })} />
          <SelectInput label={t('finance.fields.method')} value={payment.method} onChange={(method) => setPayment({ ...payment, method })} options={['CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE', 'OTHER'].map((value) => ({ value, label: t(`finance.method.${value}`) }))} />
          <TextInput label={t('finance.fields.reference')} value={payment.referenceNumber} onChange={(referenceNumber) => setPayment({ ...payment, referenceNumber })} />
        </div>
        <button className="btn btn-primary" disabled={saving === 'payment'} onClick={() => submit('payment', () => recordFeePayment({ invoiceId: Number(payment.invoiceId), amount: Number(payment.amount || 0), method: payment.method, status: 'COMPLETED', referenceNumber: payment.referenceNumber }))}>{saving === 'payment' ? t('common.saving') : t('finance.actions.recordPayment')}</button>
      </SectionCard> : null}

      {canManageCafeteria ? <SectionCard title={t('finance.forms.cafeteriaTitle')} subtitle={t('finance.forms.cafeteriaSubtitle')}>
        <div className="form-grid">
          <TextInput label={t('finance.fields.planName')} value={mealPlan.name} onChange={(name) => setMealPlan({ ...mealPlan, name })} />
          <TextInput label={t('finance.fields.pricePerMeal')} type="number" value={mealPlan.pricePerMeal} onChange={(pricePerMeal) => setMealPlan({ ...mealPlan, pricePerMeal })} />
          <TextInput label={t('finance.fields.itemName')} value={mealItem.name} onChange={(name) => setMealItem({ ...mealItem, name })} />
          <SelectInput label={t('finance.fields.mealType')} value={mealItem.mealType} onChange={(mealType) => setMealItem({ ...mealItem, mealType })} options={['BREAKFAST', 'LUNCH', 'SNACK', 'DRINK', 'OTHER'].map((value) => ({ value, label: t(`finance.mealType.${value}`) }))} />
          <TextInput label={t('finance.fields.itemPrice')} type="number" value={mealItem.price} onChange={(price) => setMealItem({ ...mealItem, price })} />
        </div>
        <div className="button-row">
          <button className="btn btn-secondary" disabled={saving === 'mealPlan'} onClick={() => submit('mealPlan', () => createMealPlan({ ...mealPlan, pricePerMeal: Number(mealPlan.pricePerMeal || 0), active: true }))}>{t('finance.actions.createMealPlan')}</button>
          <button className="btn btn-primary" disabled={saving === 'mealItem'} onClick={() => submit('mealItem', () => createMealItem({ ...mealItem, price: Number(mealItem.price || 0), available: true }))}>{t('finance.actions.createMealItem')}</button>
        </div>
      </SectionCard> : null}

      {canManageCafeteria ? <SectionCard title={t('finance.forms.mealPurchaseTitle')} subtitle={t('finance.forms.mealPurchaseSubtitle')}>
        <div className="form-grid">
          <StudentLookup label={t('finance.fields.student')} value={purchase.studentId} onChange={(studentId) => setPurchase({ ...purchase, studentId })} disabled={saving === 'purchase'} />
          <SelectInput label={t('finance.fields.mealItem')} value={purchase.mealItemId} onChange={(mealItemId) => setPurchase({ ...purchase, mealItemId })} options={[{ value: '', label: t('finance.forms.selectMealItem') }, ...mealItems.map((item) => ({ value: String(item.id), label: `${item.name} - ${money(item.price)}` }))]} />
          <SelectInput label={t('finance.fields.mealPlan')} value={purchase.mealPlanId} onChange={(mealPlanId) => setPurchase({ ...purchase, mealPlanId })} options={[{ value: '', label: t('finance.forms.noMealPlan') }, ...mealPlans.map((plan) => ({ value: String(plan.id), label: `${plan.name} - ${money(plan.pricePerMeal)}` }))]} />
          <TextInput label={t('finance.fields.quantity')} type="number" value={purchase.quantity} onChange={(quantity) => setPurchase({ ...purchase, quantity })} />
        </div>
        <button className="btn btn-primary" disabled={saving === 'purchase'} onClick={() => submit('purchase', () => recordMealPurchase({ studentId: Number(purchase.studentId), mealItemId: Number(purchase.mealItemId), mealPlanId: purchase.mealPlanId ? Number(purchase.mealPlanId) : null, quantity: Number(purchase.quantity || 1), status: 'SERVED' }))}>{saving === 'purchase' ? t('common.saving') : t('finance.actions.recordMeal')}</button>
      </SectionCard> : null}
    </div>
  );
}

function AdminInvoicePanel({ invoices, onEdit, onSaved }) {
  const { t } = useTranslation();
  const [statusFilter, setStatusFilter] = useState('');
  const [actionNotes, setActionNotes] = useState('');
  const [saving, setSaving] = useState('');

  const filteredInvoices = useMemo(() => (
    statusFilter ? invoices.filter((invoice) => invoice.status === statusFilter) : invoices
  ), [invoices, statusFilter]);

  const submitAction = async (key, action) => {
    setSaving(key);
    try {
      await action({ notes: actionNotes });
      setActionNotes('');
      await onSaved();
    } finally {
      setSaving('');
    }
  };

  return (
    <SectionCard title={t('finance.invoices.adminTitle')} subtitle={t('finance.invoices.adminSubtitle')}>
      <div className="finance-filter-row">
        <SelectInput
          label={t('finance.fields.statusFilter')}
          value={statusFilter}
          onChange={setStatusFilter}
          options={[{ value: '', label: t('finance.filters.allInvoiceStatuses') }, ...INVOICE_STATUSES.map((status) => ({ value: status, label: t(`finance.status.${status}`) }))]}
        />
        <TextInput label={t('finance.fields.actionNotes')} value={actionNotes} onChange={setActionNotes} />
      </div>

      {filteredInvoices.length ? (
        <div className="invoice-management-list">
          {filteredInvoices.map((invoice) => {
            const canCancel = Number(invoice.paidAmount || 0) <= 0 && !['CANCELLED', 'WAIVED'].includes(invoice.status);
            const canWaive = Number(invoice.balance || 0) > 0 && !['CANCELLED', 'WAIVED'].includes(invoice.status);
            const canEdit = Number(invoice.paidAmount || 0) <= 0 && !['CANCELLED', 'WAIVED', 'PAID'].includes(invoice.status);
            return (
              <article className="data-card invoice-management-card" key={invoice.id}>
                <div className="data-card-header">
                  <div>
                    <div className="data-card-title">{invoice.invoiceNumber}</div>
                    <div className="muted-copy">{invoice.studentName}</div>
                  </div>
                  <span className="badge badge-info">{t(`finance.status.${invoice.status}`)}</span>
                </div>
                <div className="data-card-meta">
                  <div className="data-card-meta-row"><span>{t('finance.fields.dueDate')}</span><span>{invoice.dueDate || '-'}</span></div>
                  <div className="data-card-meta-row"><span>{t('finance.fields.amount')}</span><span>{money(invoice.totalAmount)}</span></div>
                  <div className="data-card-meta-row"><span>{t('finance.fields.paid')}</span><span>{money(invoice.paidAmount)}</span></div>
                  <div className="data-card-meta-row"><span>{t('finance.fields.balance')}</span><span>{money(invoice.balance)}</span></div>
                </div>
                {invoice.payments?.length ? (
                  <div className="invoice-payment-list">
                    {invoice.payments.map((payment) => (
                      <div className="invoice-payment-row" key={payment.id}>
                        <div>
                          <strong>{money(payment.amount)}</strong>
                          <span>{payment.paymentDate || '-'} - {t(`finance.method.${payment.method}`, { defaultValue: payment.method })}</span>
                        </div>
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => printPaymentReceipt({ invoice, payment, t })}>{t('finance.actions.printReceipt')}</button>
                      </div>
                    ))}
                  </div>
                ) : null}
                <div className="invoice-actions">
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    disabled={!canEdit}
                    onClick={() => onEdit(invoice)}
                  >
                    {t('finance.actions.editInvoice')}
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    disabled={!canWaive || saving === `waive-${invoice.id}`}
                    onClick={() => submitAction(`waive-${invoice.id}`, (data) => waiveFeeInvoice(invoice.id, data))}
                  >
                    {saving === `waive-${invoice.id}` ? t('common.saving') : t('finance.actions.waiveInvoice')}
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled={!canCancel || saving === `cancel-${invoice.id}`}
                    onClick={() => submitAction(`cancel-${invoice.id}`, (data) => cancelFeeInvoice(invoice.id, data))}
                  >
                    {saving === `cancel-${invoice.id}` ? t('common.saving') : t('finance.actions.cancelInvoice')}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      ) : (
        <EmptyState title={t('finance.invoices.noMatches')} description={t('finance.invoices.noMatchesDescription')} />
      )}
    </SectionCard>
  );
}

function AdminCafeteriaPanel({ purchases, summaries, filters, onFilterChange }) {
  const { t } = useTranslation();
  const [draftFilters, setDraftFilters] = useState(filters);
  const totalSpend = useMemo(
    () => purchases.reduce((total, purchase) => total + Number(purchase.totalAmount || 0), 0),
    [purchases]
  );
  const totalQuantity = useMemo(
    () => purchases.reduce((total, purchase) => total + Number(purchase.quantity || 0), 0),
    [purchases]
  );

  useEffect(() => {
    setDraftFilters(filters);
  }, [filters]);

  return (
    <SectionCard title={t('finance.cafeteria.adminTitle')} subtitle={t('finance.cafeteria.adminSubtitle')}>
      <div className="finance-filter-row cafeteria-filter-row">
        <TextInput label={t('finance.fields.startDate')} type="date" value={draftFilters.startDate} onChange={(startDate) => setDraftFilters((current) => ({ ...current, startDate }))} />
        <TextInput label={t('finance.fields.endDate')} type="date" value={draftFilters.endDate} onChange={(endDate) => setDraftFilters((current) => ({ ...current, endDate }))} />
        <div className="button-row finance-filter-actions">
          <button type="button" className="btn btn-primary" onClick={() => onFilterChange(draftFilters)}>{t('finance.actions.applyFilters')}</button>
          <button type="button" className="btn btn-secondary" onClick={() => onFilterChange({ startDate: '', endDate: '' })}>{t('finance.actions.clearFilters')}</button>
        </div>
      </div>

      <div className="finance-stat-grid">
        <Stat label={t('finance.stats.filteredMealPurchases')} value={purchases.length} />
        <Stat label={t('finance.stats.filteredMealQuantity')} value={totalQuantity} />
        <Stat label={t('finance.stats.filteredCafeteriaSpend')} value={money(totalSpend)} />
      </div>

      {summaries.length ? (
        <div className="daily-summary-list">
          {summaries.map((summary) => (
            <article className="data-card" key={summary.purchaseDate}>
              <div className="data-card-header">
                <div className="data-card-title">{summary.purchaseDate}</div>
                <span className="badge badge-success">{money(summary.totalAmount)}</span>
              </div>
              <div className="data-card-meta">
                <div className="data-card-meta-row"><span>{t('finance.fields.purchases')}</span><span>{summary.purchaseCount}</span></div>
                <div className="data-card-meta-row"><span>{t('finance.fields.quantity')}</span><span>{summary.quantity}</span></div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title={t('finance.cafeteria.noSummary')} description={t('finance.cafeteria.noSummaryDescription')} />
      )}
    </SectionCard>
  );
}

function FinanceSummaryView({ summary }) {
  const { t } = useTranslation();
  return (
    <>
      <div className="finance-stat-grid">
        <Stat label={t('finance.stats.billed')} value={money(summary.billedAmount)} />
        <Stat label={t('finance.stats.paid')} value={money(summary.paidAmount)} />
        <Stat label={t('finance.stats.balance')} value={money(summary.balance)} />
      </div>

      <SectionCard title={t('finance.invoices.title')} subtitle={t('finance.invoices.subtitle')}>
        {summary.invoices?.length ? (
          <div className="desktop-table table-container">
            <table>
              <thead><tr><th>{t('finance.fields.invoice')}</th><th>{t('finance.fields.dueDate')}</th><th>{t('common.status')}</th><th>{t('finance.fields.amount')}</th><th>{t('finance.fields.paid')}</th><th>{t('finance.fields.balance')}</th></tr></thead>
              <tbody>{summary.invoices.map((invoice) => <tr key={invoice.id}><td>{invoice.invoiceNumber}</td><td>{invoice.dueDate || '-'}</td><td><span className="badge badge-info">{invoice.status}</span></td><td>{money(invoice.totalAmount)}</td><td>{money(invoice.paidAmount)}</td><td>{money(invoice.balance)}</td></tr>)}</tbody>
            </table>
          </div>
        ) : <EmptyState title={t('finance.invoices.empty')} description={t('finance.invoices.emptyDescription')} />}
      </SectionCard>
    </>
  );
}

export default function FinanceCafeteria({ role }) {
  const { t } = useTranslation();
  const { user } = useAuth();
  const childContext = useChild();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [adminData, setAdminData] = useState({ feeItems: [], invoices: [], mealPlans: [], mealItems: [], mealPurchases: [], mealPurchaseSummaries: [] });
  const [cafeteriaFilters, setCafeteriaFilters] = useState({ startDate: '', endDate: '' });
  const [editingInvoice, setEditingInvoice] = useState(null);
  const [summary, setSummary] = useState(null);
  const canManageFinance = role === 'admin' && (userHasRole(user, 'ADMIN') || userHasRole(user, 'FINANCE_STAFF'));
  const canManageCafeteria = UNIVERSITY_DEMO_CAFETERIA_ENABLED && role === 'admin' && (userHasRole(user, 'ADMIN') || userHasRole(user, 'CAFETERIA_STAFF'));

  const selectedStudentId = useMemo(() => {
    if (role === 'student') return user?.id;
    if (role === 'parent') return childContext?.selectedChild?.id;
    return null;
  }, [childContext?.selectedChild?.id, role, user?.id]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      if (role === 'admin') {
        const purchaseParams = {
          startDate: cafeteriaFilters.startDate || undefined,
          endDate: cafeteriaFilters.endDate || undefined,
        };
        const [feeItemsRes, invoicesRes, mealPlansRes, mealItemsRes, mealPurchasesRes, mealPurchaseSummariesRes] = await Promise.all([
          canManageFinance ? getFeeItems() : Promise.resolve({ data: [] }),
          canManageFinance ? getFeeInvoices() : Promise.resolve({ data: [] }),
          canManageCafeteria ? getMealPlans() : Promise.resolve({ data: [] }),
          canManageCafeteria ? getMealItems() : Promise.resolve({ data: [] }),
          canManageCafeteria ? getMealPurchases(purchaseParams) : Promise.resolve({ data: [] }),
          canManageCafeteria ? getMealPurchaseDailySummary(purchaseParams) : Promise.resolve({ data: [] }),
        ]);
        setAdminData({
          feeItems: feeItemsRes.data || [],
          invoices: invoicesRes.data || [],
          mealPlans: mealPlansRes.data || [],
          mealItems: mealItemsRes.data || [],
          mealPurchases: mealPurchasesRes.data || [],
          mealPurchaseSummaries: mealPurchaseSummariesRes.data || [],
        });
      } else if (selectedStudentId) {
        const res = await getStudentFinanceSummary(selectedStudentId);
        setSummary(res.data);
      }
    } catch (err) {
      console.error('Failed to load finance data', err);
      setError(err.response?.data?.message || t('finance.loadErrorDescription'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (role === 'parent' && childContext?.loading) return;
    load();
  }, [role, selectedStudentId, childContext?.loading, cafeteriaFilters, canManageFinance, canManageCafeteria]);

  if (role === 'parent' && childContext?.loading) return <LoadingState label={t('finance.loading')} />;
  if (loading) return <LoadingState label={t('finance.loading')} />;
  if (error) return <ErrorState title={t('finance.loadErrorTitle')} description={error} retryLabel={t('admin.users.retry')} onRetry={load} />;
  if (role === 'parent' && !selectedStudentId) return <EmptyState title={t('parent.grades.noChildren')} description={t('finance.noStudentDescription')} />;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{role === 'admin' ? t('finance.adminTitle') : t('finance.familyTitle', { name: summary?.studentName || '' })}</h1>
          <p className="muted-copy">{role === 'admin' ? t('finance.adminSummary') : t('finance.familySummary')}</p>
        </div>
      </div>
      {role === 'parent' ? <ChildSelector /> : null}
      {role === 'admin' ? (
        <>
          <div className="finance-stat-grid">
            {canManageFinance ? <Stat label={t('finance.stats.openInvoices')} value={adminData.invoices.length} /> : null}
            {canManageFinance ? <Stat label={t('finance.stats.feeItems')} value={adminData.feeItems.length} /> : null}
            {canManageCafeteria ? <Stat label={t('finance.stats.mealItems')} value={adminData.mealItems.length} /> : null}
            {canManageCafeteria ? <Stat label={t('finance.stats.mealPurchases')} value={adminData.mealPurchases.length} /> : null}
          </div>
          <AdminFinanceForms {...adminData} editingInvoice={editingInvoice} canManageFinance={canManageFinance} canManageCafeteria={canManageCafeteria} onCancelEdit={() => setEditingInvoice(null)} onSaved={load} />
          {canManageFinance ? <AdminInvoicePanel invoices={adminData.invoices} onEdit={setEditingInvoice} onSaved={load} /> : null}
          {canManageCafeteria ? <AdminCafeteriaPanel purchases={adminData.mealPurchases} summaries={adminData.mealPurchaseSummaries} filters={cafeteriaFilters} onFilterChange={setCafeteriaFilters} /> : null}
        </>
      ) : <FinanceSummaryView summary={summary || {}} />}
    </div>
  );
}
