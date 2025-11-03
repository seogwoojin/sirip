# 시맆 리워드 패스 모노레포

이 저장소는 Spring Boot 기반 백엔드와 React 기반 프런트엔드를 각각 `backend`, `frontend` 디렉터리로 분리하고, Docker Compose로 한 번에 실행할 수 있도록 구성되었습니다.

## 실행 방법

1. Docker와 Docker Compose가 설치되어 있어야 합니다.
2. 저장소 루트에서 다음 명령을 실행합니다.

```bash
docker compose up --build
```

3. 서비스가 기동되면 다음 주소에서 접속할 수 있습니다.
   - 프런트엔드: http://localhost:5173
   - 백엔드 API: http://localhost:8080

프런트엔드는 `VITE_API_BASE_URL` 환경 변수를 통해 백엔드 주소를 참조하며, Compose 환경에서는 자동으로 백엔드 컨테이너를 바라보도록 설정되어 있습니다.
