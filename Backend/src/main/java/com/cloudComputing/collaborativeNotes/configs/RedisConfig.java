package com.cloudComputing.collaborativeNotes.configs;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static final Dotenv dotenv = Dotenv.load();

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Leer configuración desde variables de entorno o .env
        String redisHost = dotenv.get("REDIS_HOST");
        int redisPort = Integer.parseInt(dotenv.get("REDIS_PORT", "6379"));
        boolean useTls = Boolean.parseBoolean(dotenv.get("REDIS_USE_TLS", "true"));
        String redisPassword = dotenv.get("REDIS_PASSWORD");

        // Configuración de Redis
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        // Configurar cliente Redis con TLS si es necesario
        LettuceClientConfiguration clientConfig;
        if (useTls) {
            clientConfig = LettuceClientConfiguration.builder()
                    .useSsl() // Habilita TLS
                    .build();
        } else {
            clientConfig = LettuceClientConfiguration.builder().build();
        }

        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}