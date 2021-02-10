package com.mobinspect.dynamicdq.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;


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
}
