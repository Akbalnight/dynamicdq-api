package com.mobinspect.dynamicdq.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;


@Getter @Setter
@NoArgsConstructor
@Component
public class JacksonConverter {

   private static ObjectMapper mapper = new ObjectMapper();

    public static JsonNode objectToJson(Object o) {
         return mapper.valueToTree(o);
    }

    public static Object jsonToObject(TreeNode node, Class<Object> c) throws JsonProcessingException {
        return mapper.treeToValue(node, c);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));
        return factory.createMultipartConfig();
    }
}
