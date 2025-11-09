import React, { useEffect, useMemo, useState } from "react";


// Vite does not support `import.meta as any` in plain JS files.
// We switch to a safer runtime check.
const API_BASE_URL = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_API_BASE_URL) || "http://localhost:8080";


function classNames(...xs) {
return xs.filter(Boolean).join(" ");
}

function usePersistedState(key, initial) {
  const [v, setV] = useState(() => {
    try {
      const s = localStorage.getItem(key);
      return s !== null ? JSON.parse(s) : initial;
    } catch {
      return initial;
    }
  });
  useEffect(() => {
    try { localStorage.setItem(key, JSON.stringify(v)); } catch {}
  }, [key, v]);
  return [v, setV];
}

async function apiFetch(path, { token, method = "GET", query, body } = {}) {
  const url = new URL(API_BASE_URL + path);
  if (query) {
    Object.entries(query).forEach(([k, val]) => {
      if (val !== undefined && val !== null && val !== "") url.searchParams.set(k, String(val));
    });
  }
  const resp = await fetch(url.toString(), {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
    credentials: "include",
  });
  if (!resp.ok) {
    const text = await resp.text().catch(() => "");
    throw new Error(`${resp.status} ${resp.statusText}\n${text}`.trim());
  }
  const ct = resp.headers.get("content-type") || "";
  if (ct.includes("application/json")) return resp.json();
  return resp.text();
}

function Card({ title, children, footer, className }) {
  return (
    <div className={classNames("rounded-2xl border p-4 shadow-sm bg-white/80", className)}>
      {title && <div className="mb-2 text-lg font-semibold">{title}</div>}
      <div>{children}</div>
      {footer && <div className="mt-3 text-sm text-gray-500">{footer}</div>}
    </div>
  );
}

function Toolbar({ tabs, value, onChange, right }) {
  return (
    <div className="flex items-center justify-between gap-2">
      <div className="inline-flex rounded-xl border bg-white/70 p-1">
        {tabs.map((t) => (
          <button
            key={t.value}
            className={classNames(
              "px-3 py-1.5 rounded-lg text-sm",
              value === t.value ? "bg-black text-white" : "hover:bg-gray-100"
            )}
            onClick={() => onChange(t.value)}
          >
            {t.label}
          </button>
        ))}
      </div>
      <div className="flex items-center gap-2">{right}</div>
    </div>
  );
}

function Labeled({ label, children, hint }) {
  return (
    <label className="flex flex-col gap-1 text-sm">
      <span className="text-gray-700">{label}</span>
      {children}
      {hint && <span className="text-xs text-gray-400">{hint}</span>}
    </label>
  );
}

function TextInput(props) {
  return (
    <input
      {...props}
      className={classNames(
        "w-full rounded-lg border px-3 py-2 outline-none focus:ring-2 focus:ring-black/30",
        props.className
      )}
    />
  );
}

function NumberInput(props) {
  return <TextInput type="number" inputMode="numeric" {...props} />;
}

function DateTimeInput(props) {
  return <TextInput type="datetime-local" {...props} />;
}

function PrimaryButton({ children, ...rest }) {
  return (
    <button
      {...rest}
      className={classNames(
        "rounded-lg bg-black px-3 py-2 text-white hover:bg-black/90 disabled:opacity-50",
        rest.className
      )}
    >
      {children}
    </button>
  );
}

function GhostButton({ children, ...rest }) {
  return (
    <button
      {...rest}
      className={classNames(
        "rounded-lg border px-3 py-2 hover:bg-gray-50",
        rest.className
      )}
    >
      {children}
    </button>
  );
}

function useToast() {
  const [msg, setMsg] = useState("");
  const [type, setType] = useState("info");
  const show = (m, t = "info") => { setMsg(m); setType(t); setTimeout(() => setMsg(""), 2500); };
  return { Toast: () => msg ? (
    <div className={classNames(
      "fixed bottom-4 left-1/2 -translate-x-1/2 rounded-xl px-4 py-2 shadow-md",
      type === "error" ? "bg-red-600 text-white" : "bg-gray-900 text-white"
    )}>{msg}</div>
  ) : null, show };
}

// --- Auth Forms ---
function SignupForm({ onDone }) {
  const { show, Toast } = useToast();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const submit = async (e) => {
    e.preventDefault();
    try {
      const res = await apiFetch("/user", { method: "POST", body: { email, password } });
      show(`가입 완료: ${res?.result ?? "OK"}`);
      onDone?.();
    } catch (err) {
      show(String(err.message || err), "error");
    }
  };
  return (
    <Card title="회원가입">
      <form onSubmit={submit} className="grid gap-3">
        <Labeled label="Email"><TextInput value={email} onChange={(e) => setEmail(e.target.value)} required /></Labeled>
        <Labeled label="Password"><TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></Labeled>
        <PrimaryButton type="submit">Sign up</PrimaryButton>
      </form>
      <Toast />
    </Card>
  );
}

function LoginForm({ setToken }) {
  const { show, Toast } = useToast();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    try {
      const res = await apiFetch("/user/login", { method: "POST", body: { email, password } });
      const token = res?.accessToken;
      if (!token) throw new Error("토큰이 응답에 없습니다.");
      setToken(token);
      show("로그인 성공");
    } catch (err) {
      show(String(err.message || err), "error");
    }
  };

  return (
    <Card title="로그인">
      <form onSubmit={submit} className="grid gap-3">
        <Labeled label="Email"><TextInput value={email} onChange={(e) => setEmail(e.target.value)} required /></Labeled>
        <Labeled label="Password"><TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} required /></Labeled>
        <PrimaryButton type="submit">Log in</PrimaryButton>
      </form>
      <Toast />
    </Card>
  );
}

// --- Event List & Apply ---
function EventsList({ token, accountId }) {
  const { show, Toast } = useToast();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await apiFetch("/api/events", { token });
      setEvents(data || []);
    } catch (e) {
      show(String(e.message || e), "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [token]);

  const apply = async (eventId) => {
    try {
      const res = await apiFetch(`/api/events/${eventId}/coupons`, {
        token,
        method: "POST",
        query: { accountId },
      });
      show(`신청 완료 (즉시 발급: ${res?.issuedImmediately ? "예" : "아니오"})`);
      load();
    } catch (e) {
      show(String(e.message || e), "error");
    }
  };

  return (
    <Card title="행사 목록">
      <div className="mb-3 flex items-center gap-2">
        <GhostButton onClick={load} disabled={loading}>{loading ? "로딩..." : "새로고침"}</GhostButton>
      </div>
      <div className="grid gap-3">
        {events?.length ? events.map((ev) => (
          <div key={ev.id} className="rounded-xl border p-3">
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="text-base font-semibold">{ev.title}</div>
                <div className="text-sm text-gray-600">{ev.description}</div>
                <div className="text-sm">리워드: {ev.rewardDescription}</div>
                <div className="text-xs text-gray-500">{fmtDate(ev.startAt)} ~ {fmtDate(ev.endAt)} / 총 {ev.totalCoupons} / 잔여 {ev.remainingCoupons} / {ev.active ? '진행' : '종료'}</div>
              </div>
              <PrimaryButton onClick={() => apply(ev.id)} disabled={!accountId}>신청</PrimaryButton>
            </div>
          </div>
        )) : <div className="text-sm text-gray-500">표시할 행사가 없습니다.</div>}
      </div>
      <Toast />
    </Card>
  );
}

// --- My Coupons ---
function MyCoupons({ token, accountId }) {
  const { show, Toast } = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await apiFetch("/api/users/me/coupons", { token, query: { accountId } });
      setItems(data || []);
    } catch (e) {
      show(String(e.message || e), "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { if (accountId) load(); }, [token, accountId]);

  const redeem = async (couponId) => {
    try {
      await apiFetch(`/api/coupons/${couponId}/redeem`, { token, method: "POST", query: { accountId } });
      show("사용 처리 완료");
      load();
    } catch (e) { show(String(e.message || e), "error"); }
  };

  const noShow = async (couponId) => {
    try {
      await apiFetch(`/api/coupons/${couponId}/no-show`, { token, method: "POST", query: { accountId } });
      show("노쇼 처리 완료");
      load();
    } catch (e) { show(String(e.message || e), "error"); }
  };

  return (
    <Card title="내 쿠폰">
      <div className="mb-3 flex items-center gap-2">
        <GhostButton onClick={load} disabled={loading || !accountId}>{loading ? "로딩..." : "새로고침"}</GhostButton>
      </div>
      <div className="grid gap-3">
        {items?.length ? items.map((c) => (
          <div key={c.couponId} className="rounded-xl border p-3">
            <div className="flex items-start justify-between gap-3">
              <div className="text-sm">
                <div className="font-medium">쿠폰 #{c.couponId} (이벤트 #{c.eventId})</div>
                <div>상태: <b>{c.status}</b>{typeof c.queuePosition === 'number' ? ` / 대기번호 ${c.queuePosition}` : ''}</div>
                <div className="text-gray-500">발급: {fmtDate(c.issuedAt)} / 신청: {fmtDate(c.appliedAt)}</div>
              </div>
              <div className="flex gap-2">
                <PrimaryButton onClick={() => redeem(c.couponId)} disabled={c.status !== 'ISSUED'}>사용(검증)</PrimaryButton>
                <GhostButton onClick={() => noShow(c.couponId)} disabled={c.status !== 'ISSUED'}>노쇼</GhostButton>
              </div>
            </div>
          </div>
        )) : <div className="text-sm text-gray-500">쿠폰이 없습니다.</div>}
      </div>
      <Toast />
    </Card>
  );
}

// --- Admin Panel ---
function AdminPanel({ token, accountId }) {
  const [tab, setTab] = useState("create");

  return (
    <Card title="관리자">
      <Toolbar
        tabs={[{label: "생성", value: "create"}, {label: "스케줄 수정", value: "sched"}, {label: "리워드 수정", value: "reward"}, {label: "이벤트 조회", value: "get"}]}
        value={tab}
        onChange={setTab}
      />
      <div className="mt-4">
        {tab === "create" && <CreateEventForm token={token} accountId={accountId} />}
        {tab === "sched" && <UpdateScheduleForm token={token} accountId={accountId} />}
        {tab === "reward" && <UpdateRewardForm token={token} accountId={accountId} />}
        {tab === "get" && <GetEventForm token={token} accountId={accountId} />}
      </div>
    </Card>
  );
}

function CreateEventForm({ token, accountId }) {
  const { show, Toast } = useToast();
  const [form, setForm] = useState({ title: "", description: "", rewardDescription: "", totalCoupons: 100, startAt: "", endAt: "" });

  const submit = async (e) => {
    e.preventDefault();
    try {
      const body = { ...form, totalCoupons: Number(form.totalCoupons), startAt: toISO(form.startAt), endAt: toISO(form.endAt) };
      const res = await apiFetch("/api/admin/events", { token, method: "POST", query: { accountId }, body });
      show(`이벤트 생성됨: #${res?.id ?? ""}`);
      setForm({ title: "", description: "", rewardDescription: "", totalCoupons: 100, startAt: "", endAt: "" });
    } catch (e) { show(String(e.message || e), "error"); }
  };

  return (
    <form onSubmit={submit} className="grid gap-3">
      <Labeled label="제목"><TextInput value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></Labeled>
      <Labeled label="설명"><TextInput value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} required /></Labeled>
      <Labeled label="리워드 설명"><TextInput value={form.rewardDescription} onChange={(e) => setForm({ ...form, rewardDescription: e.target.value })} required /></Labeled>
      <div className="grid grid-cols-2 gap-3 max-sm:grid-cols-1">
        <Labeled label="총 쿠폰 수"><NumberInput value={form.totalCoupons} onChange={(e) => setForm({ ...form, totalCoupons: e.target.value })} min={0} required /></Labeled>
        <div></div>
        <Labeled label="시작 시각"><DateTimeInput value={form.startAt} onChange={(e) => setForm({ ...form, startAt: e.target.value })} required /></Labeled>
        <Labeled label="종료 시각"><DateTimeInput value={form.endAt} onChange={(e) => setForm({ ...form, endAt: e.target.value })} required /></Labeled>
      </div>
      <PrimaryButton type="submit" disabled={!accountId}>이벤트 생성</PrimaryButton>
      <Toast />
    </form>
  );
}

function UpdateScheduleForm({ token, accountId }) {
  const { show, Toast } = useToast();
  const [eventId, setEventId] = useState("");
  const [startAt, setStartAt] = useState("");
  const [endAt, setEndAt] = useState("");
  const submit = async (e) => {
    e.preventDefault();
    try {
      await apiFetch(`/api/admin/events/${eventId}/schedule`, {
        token,
        method: "PATCH",
        query: { accountId },
        body: { startAt: toISO(startAt), endAt: toISO(endAt) },
      });
      show("스케줄 업데이트 완료");
      setEventId(""); setStartAt(""); setEndAt("");
    } catch (e) { show(String(e.message || e), "error"); }
  };
  return (
    <form onSubmit={submit} className="grid gap-3">
      <Labeled label="이벤트 ID"><NumberInput value={eventId} onChange={(e) => setEventId(e.target.value)} required /></Labeled>
      <div className="grid grid-cols-2 gap-3 max-sm:grid-cols-1">
        <Labeled label="시작 시각"><DateTimeInput value={startAt} onChange={(e) => setStartAt(e.target.value)} required /></Labeled>
        <Labeled label="종료 시각"><DateTimeInput value={endAt} onChange={(e) => setEndAt(e.target.value)} required /></Labeled>
      </div>
      <PrimaryButton type="submit" disabled={!accountId}>업데이트</PrimaryButton>
      <Toast />
    </form>
  );
}

function UpdateRewardForm({ token, accountId }) {
  const { show, Toast } = useToast();
  const [eventId, setEventId] = useState("");
  const [rewardDescription, setRewardDescription] = useState("");
  const submit = async (e) => {
    e.preventDefault();
    try {
      await apiFetch(`/api/admin/events/${eventId}/reward`, { token, method: "PATCH", query: { accountId }, body: { rewardDescription } });
      show("리워드 설명 업데이트 완료");
      setEventId(""); setRewardDescription("");
    } catch (e) { show(String(e.message || e), "error"); }
  };
  return (
    <form onSubmit={submit} className="grid gap-3">
      <Labeled label="이벤트 ID"><NumberInput value={eventId} onChange={(e) => setEventId(e.target.value)} required /></Labeled>
      <Labeled label="리워드 설명"><TextInput value={rewardDescription} onChange={(e) => setRewardDescription(e.target.value)} required /></Labeled>
      <PrimaryButton type="submit" disabled={!accountId}>업데이트</PrimaryButton>
      <Toast />
    </form>
  );
}

function GetEventForm({ token, accountId }) {
  const { show, Toast } = useToast();
  const [eventId, setEventId] = useState("");
  const [data, setData] = useState(null);
  const submit = async (e) => {
    e.preventDefault();
    try {
      const res = await apiFetch(`/api/admin/events/${eventId}`, { token, query: { accountId } });
      setData(res);
    } catch (e) { show(String(e.message || e), "error"); setData(null); }
  };
  return (
    <div className="grid gap-3">
      <form onSubmit={submit} className="grid gap-3">
        <Labeled label="이벤트 ID"><NumberInput value={eventId} onChange={(e) => setEventId(e.target.value)} required /></Labeled>
        <PrimaryButton type="submit" disabled={!accountId}>조회</PrimaryButton>
      </form>
      {data && (
        <div className="rounded-xl border p-3 text-sm">
          <div className="font-medium">#{data.id} - {data.title}</div>
          <div className="text-gray-700">{data.description}</div>
          <div>리워드: {data.rewardDescription}</div>
          <div className="text-gray-500">{fmtDate(data.startAt)} ~ {fmtDate(data.endAt)} / 총 {data.totalCoupons} / 잔여 {data.remainingCoupons} / {data.active ? '진행' : '종료'}</div>
        </div>
      )}
      <Toast />
    </div>
  );
}

function fmtDate(s) {
  if (!s) return "-";
  try {
    const d = new Date(s);
    if (Number.isNaN(d.getTime())) return s;
    return d.toLocaleString();
  } catch { return s; }
}

function toISO(localValue) {
  if (!localValue) return null;
  // input type=datetime-local gives 'YYYY-MM-DDTHH:mm'
  const dt = new Date(localValue);
  if (Number.isNaN(dt.getTime())) {
    // Safari handling: treat as local without timezone
    return localValue + ":00Z"; // best-effort
  }
  return dt.toISOString();
}

export default function App() {
  const [token, setToken] = usePersistedState("token", "");
  const [accountId, setAccountId] = usePersistedState("accountId", "");
  const [tab, setTab] = useState("events");

  const logout = () => { setToken(""); };

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 to-gray-100 text-gray-900">
      <div className="mx-auto max-w-5xl p-4 md:p-8">
        <header className="mb-6 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold">Sirip Reward Front</h1>
            <div className="text-xs text-gray-500">API: {API_BASE_URL}</div>
          </div>
          <div className="flex items-center gap-2">
            <Labeled label="Account ID">
              <TextInput placeholder="예: 1" value={accountId} onChange={(e) => setAccountId(e.target.value)} style={{ width: 140 }} />
            </Labeled>
            {token ? (
              <>
                <span className="text-xs text-emerald-700">로그인됨</span>
                <GhostButton onClick={logout}>Logout</GhostButton>
              </>
            ) : null}
          </div>
        </header>

        {!token ? (
          <div className="grid gap-4 md:grid-cols-2">
            <LoginForm setToken={setToken} />
            <SignupForm onDone={() => {}} />
          </div>
        ) : (
          <div className="grid gap-4">
            <Toolbar
              tabs={[
                { label: "행사", value: "events" },
                { label: "내 쿠폰", value: "coupons" },
                { label: "관리자", value: "admin" },
              ]}
              value={tab}
              onChange={setTab}
              right={<span className="text-xs text-gray-500">Bearer 토큰 보관 중</span>}
            />

            {tab === "events" && <EventsList token={token} accountId={accountId} />}
            {tab === "coupons" && <MyCoupons token={token} accountId={accountId} />}
            {tab === "admin" && <AdminPanel token={token} accountId={accountId} />}
          </div>
        )}

        <footer className="mt-10 text-center text-xs text-gray-400">
          Built for the provided OpenAPI • {new Date().getFullYear()}
        </footer>
      </div>
    </div>
  );
}
