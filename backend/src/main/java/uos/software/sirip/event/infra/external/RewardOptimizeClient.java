package uos.software.sirip.event.infra.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RewardOptimizeClient {

    private final WebClient aiWebClient;

    public double optimizeReward(OptimizeRequest req) {
        OptimizeResponse response = aiWebClient.post()
                .uri("/optimize")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(OptimizeResponse.class)
                .block(); // 동기 호출

        if (response == null) {
            throw new IllegalStateException("AI 응답이 비어 있습니다.");
        }

        return response.getRecommended_reward();
    }
}
