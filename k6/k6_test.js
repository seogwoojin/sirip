import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';

// ✅ 유저 100명 정의
const users = [];
for (let i = 20; i < 100; i++) {
  users.push({
    email: `admin${i}$@uos.ac.kr`,
    password: 'encoded_password',
  });
}

// ✅ 사전 로그인 (모든 유저의 토큰 저장)
export function setup() {
  const tokens = users.map((user) => {
    const payload = JSON.stringify({
      email: user.email,
      password: user.password,
    });

    const res = http.post(`${BASE_URL}/user/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });

    if (res.status === 200 && res.json('accessToken')) {
      return res.json('accessToken');
    } else {
      console.error(`❌ 로그인 실패: ${user.email} (${res.status})`);
      return null;
    }
  });

  return { tokens };
}

// ✅ 모든 유저가 동시에 1회 발급 시도
export const options = {
  vus: 80,          // 가상 유저 수 = 실제 유저 수
  iterations: 80,   // 각 유저가 딱 1번만 실행
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1000'],
  },
};

// ✅ 쿠폰 발급 (1회 한정)
export default function (data) {
  const token = data.tokens[__VU - 1]; // 각 VU는 고유 유저 사용
  if (!token) return;

  const res = http.post(`${BASE_URL}/api/events/5/coupons`, null, {
    headers: { Authorization: `Bearer ${token}` },
  });

  check(res, {
    '쿠폰 발급 성공 (200|201)': (r) => r.status === 200 || r.status === 201,
    '쿠폰 중복 거절 (409)': (r) => r.status === 409 || r.status === 400,
  });
}