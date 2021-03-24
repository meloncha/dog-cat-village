package donation.pet.config;

import donation.pet.socket.SignalingHandler;
import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

        @Bean
        public SignalingHandler signalHandler() {
            return new SignalingHandler();
        }

        @Bean
        public KurentoClient kurentoClient() {
            return KurentoClient.create();
        }

        @Bean
        public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
            ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
            container.setMaxTextMessageBufferSize(32768);
            return container;
        }

        // WebSocket를 등록 (wss:// + localhost + /call)
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(signalHandler(), "/call");
        }
}
