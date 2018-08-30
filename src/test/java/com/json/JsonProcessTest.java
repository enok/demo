package com.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcessTest {

    static {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.of(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL);
            }
        });
    }

    private JsonProcess jsonProcess;

    @Before
    public void setUp() throws Exception {
        jsonProcess = new JsonProcess();
    }

    @Test
    public void setValueIntoExistentNode() {
        Object value = "avenue";
        String jsonTarget = "{\"order\":{\"number\":123,\"person\":{\"address\":{\"type\":\"street\"}}}}";
        String jsonResult = "{\"order\":{\"number\":123,\"person\":{\"address\":{\"type\":\"avenue\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.person.address.type", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentNode() {
        Object value = "Michigan Avenue";
        String jsonTarget = "{\"order\":{\"number\":123,\"person\":{\"address\":{\"type\":\"street\"}}}}";
        String jsonResult = "{\"order\":{\"number\":123,\"person\":{\"address\":{\"type\":\"street\",\"name\":\"Michigan Avenue\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.person.address.name", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentNodeSeveralNodes() {
        Object value = "Michigan Avenue";
        String jsonTarget = "{\"order\": {\"number\": 123,\"person\": {}}}";
        String jsonResult = "{\"order\":{\"number\":123,\"person\":{\"address\":{\"name\":\"Michigan Avenue\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.person.address.name", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArray() {
        Object value = "754-3010";
        String jsonTarget = "{\"contacts\":[{\"name\":\"Luther King\",\"phone-number\":\"754-4020\"}]}";
        String jsonResult = "{\"contacts\":[{\"name\":\"Luther King\",\"phone-number\":\"754-3010\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contacts[0].phone-number", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void addValueIntoExistentArray() {
        Object value = "Luther King";
        String jsonTarget = "{\"contacts\":[{\"phone-number\":\"754-4020\"}]}";
        String jsonResult = "{\"contacts\":[{\"phone-number\":\"754-4020\",\"name\":\"Luther King\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contacts[0].name", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentArray() {
        Object value = "754-3010";
        String jsonTarget = "{}";
        String jsonResult = "{\"contacts\":[{\"phone-number\":\"754-3010\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contacts[0].phone-number", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentArrayMoreComplex() {
        Object value = "JOSEPH";
        String jsonTarget = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\"}}],\"customers\":{\"customer\":{\"person-type\":\"LP\"}}}}";
        String jsonResult = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\"}}],\"customers\":{\"customer\":{\"person-type\":\"LP\",\"contact\":[{\"name\":\"JOSEPH\"}]}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.customers.customer.contact[0].name", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArrayMoreComplex() {
        Object value = "MARY";
        String jsonTarget = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\"}}],\"customers\":{\"customer\":{\"person-type\":\"LP\",\"contact\":[{\"name\":\"JOSEPH\"}]}}}}";
        String jsonResult = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\"}}],\"customers\":{\"customer\":{\"person-type\":\"LP\",\"contact\":[{\"name\":\"JOSEPH\"},{\"name\":\"MARY\"}]}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.customers.customer.contact[1].name", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArrayEvenMoreComplex() {
        Object value = "book";
        String jsonTarget = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\"}}],\"customer\":{\"address\":{\"name\":\"Michigan Avenue\"}}}}";
        String jsonResult = "{\"order\":{\"order-item\":[{\"product\":{\"id\":\"112233\",\"group\":{\"type\":\"book\"}}}],\"customer\":{\"address\":{\"name\":\"Michigan Avenue\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.order-item[0].product.group.type", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArrayIntoAnotherArray() {
        Object value = "5678";
        String jsonTarget = "{\"order\":{\"order-item\":[{\"product\":{\"product-item\":null}}]}}";
        String jsonResult = "{\"order\":{\"order-item\":[{\"product\":{\"product-item\":[{\"id\":\"5678\"}]}}]}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.order.order-item[0].product.product-item[0].id", value);

        assertThat(result, is(equalTo(jsonResult)));
    }
}