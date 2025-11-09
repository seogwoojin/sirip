import { useEffect, useMemo, useState } from 'react';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

const sections = [
  {
    title: '디지털 리워드 쿠폰',
    description:
      '포털 인증 기반으로 1인 1매만 발급되며, 현장에서 QR 검증을 통해 공정하게 사용됩니다.',
  },
  {
    title: '행사 및 리워드 모아보기',
    description:
      '참여 가능한 모든 행사와 내가 신청한 리워드를 한 화면에서 확인할 수 있습니다.',
  },
  {
    title: '선착순/한정 쿠폰',
    description: '대기열과 잔여량 안내를 통해 누구에게나 공평한 참여 기회를 제공합니다.',
  },
  {
    title: '실시간 리워드 변경',
    description: '참여도와 일정에 따라 보상을 탄력적으로 조정할 수 있습니다.',
  },
];

export default function App() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await axios.get(`${API_BASE_URL}/api/events`);
        setEvents(response.data ?? []);
      } catch (err) {
        setError('행사 정보를 불러오는 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.');
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []);

  const eventContent = useMemo(() => {
    if (loading) {
      return <p className="muted">행사 정보를 불러오는 중입니다…</p>;
    }

    if (error) {
      return <p className="error">{error}</p>;
    }

    if (events.length === 0) {
      return <p className="muted">현재 공개된 행사가 없습니다. 새로운 행사를 기대해 주세요!</p>;
    }

    return (
      <ul className="event-list">
        {events.map((event) => (
          <li key={event.id} className="event-card">
            <h3>{event.title}</h3>
            <p>{event.description}</p>
            <dl>
              <div>
                <dt>기간</dt>
                <dd>
                  {event.startDate} ~ {event.endDate}
                </dd>
              </div>
              <div>
                <dt>잔여 쿠폰</dt>
                <dd>{event.remainingCoupons}</dd>
              </div>
              <div>
                <dt>참여 상태</dt>
                <dd>{event.active ? '진행 중' : '종료'}</dd>
              </div>
            </dl>
          </li>
        ))}
      </ul>
    );
  }, [events, loading, error]);

  return (
    <div className="page">
      <header className="hero">
        <h1>시맆 리워드 패스</h1>
        <p>
          종이 쿠폰 대신 디지털 리워드 시스템으로 투명하고 편리한 행사 운영을 경험하세요.
        </p>
      </header>
      <main>
        <section className="features">
          <h2>주요 기능</h2>
          <div className="feature-grid">
            {sections.map((section) => (
              <article key={section.title} className="feature-card">
                <h3>{section.title}</h3>
                <p>{section.description}</p>
              </article>
            ))}
          </div>
        </section>
        <section className="events">
          <h2>참여 가능한 행사</h2>
          {eventContent}
        </section>
      </main>
      <footer>
        <small>© {new Date().getFullYear()} 시맆 리워드 패스</small>
      </footer>
    </div>
  );
}
