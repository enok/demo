package com.example.demo;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by enokj on 21/08/2018
 */
public class JsonProcessTest {

    private JsonProcess jsonProcess;

    @Before
    public void setUp() throws Exception {
        jsonProcess = new JsonProcess();
    }

    @Test
    public void salvaValorEmNoExistente() {
        Object value = "alameda";
        String jsonTarget = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\"}}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"alameda\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(value, jsonTarget, "$.pedido.pessoa.endereco.tipo");

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void salvaValorEmNoNaoExistente() {
        Object value = "23 DE MAIO";
        String jsonTarget = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\"}}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\",\"logradouro\":\"23 DE MAIO\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(value, jsonTarget, "$.pedido.pessoa.endereco.logradouro");

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void salvaValorEmNoNaoExistenteVariosNos() {
        Object value = "23 DE MAIO";
        String jsonTarget = "{\"pedido\": {\"numero\": 123,\"pessoa\": {}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"logradouro\":\"23 DE MAIO\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(value, jsonTarget, "$.pedido.pessoa.endereco.logradouro");

        assertThat(result, is(equalTo(jsonResult)));
    }
}