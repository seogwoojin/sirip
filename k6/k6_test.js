import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';

// 유저 20명
const users = [];
for (let i = 0; i < 200; i++) {
  users.push({
    email: `admin${i}@uos.ac.kr`,
    password: 'encoded_password',
  });
}

// 로그인
export function setup() {
  const tokens = users.map((user) => {
    const payload = JSON.stringify({
      email: user.email,
      password: user.password,
    });

    const res = http.post(`${BASE_URL}/user/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
    });

    return res.json('accessToken');
  });

  return { tokens };
}

export const options = {
  vus: 200,
  iterations: 200,
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1000'],
  },
};

// 발급
export default function (data) {
  const token = data.tokens[__VU - 1];
  if (!token) return;

  const res = http.post(`${BASE_URL}/api/events/13/coupons`, null, {
    headers: { Authorization: `Bearer ${token}` },
  });

  check(res, {
    '200 또는 201 성공': (r) => r.status === 200 || r.status === 201,
  });
}
