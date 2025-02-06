package com.cloudComputing.collaborativeNotes.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.ssl:false}")
    private boolean useTls;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Configuración de Redis
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);

        // Configuración del cliente Lettuce con TLS si es necesario
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
                LettuceClientConfiguration.builder();

        if (useTls) {
            clientConfigBuilder.useSsl();
            System.out.println("✅ Redis usando TLS");
        } else {
            System.out.println("🚨 Redis SIN TLS");
        }

        LettuceClientConfiguration clientConfig = clientConfigBuilder.build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
