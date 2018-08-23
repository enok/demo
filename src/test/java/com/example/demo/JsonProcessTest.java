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
    public void setValueIntoExistentNode() {
        Object value = "alameda";
        String jsonTarget = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\"}}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"alameda\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.pessoa.endereco.tipo", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentNode() {
        Object value = "23 DE MAIO";
        String jsonTarget = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\"}}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"tipo\":\"rua\",\"logradouro\":\"23 DE MAIO\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.pessoa.endereco.logradouro", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentNodeSeveralNodes() {
        Object value = "23 DE MAIO";
        String jsonTarget = "{\"pedido\": {\"numero\": 123,\"pessoa\": {}}}";
        String jsonResult = "{\"pedido\":{\"numero\":123,\"pessoa\":{\"endereco\":{\"logradouro\":\"23 DE MAIO\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.pessoa.endereco.logradouro", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArray() {
        Object value = "9988776655";
        String jsonTarget = "{\"contatos\":[{\"nome\":\"GOKU\",\"telefone\":\"1122334455\"}]}";
        String jsonResult = "{\"contatos\":[{\"nome\":\"GOKU\",\"telefone\":\"9988776655\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contatos[0].telefone", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void addValueIntoExistentArray() {
        Object value = "GOKU";
        String jsonTarget = "{\"contatos\":[{\"telefone\":\"1122334455\"}]}";
        String jsonResult = "{\"contatos\":[{\"telefone\":\"1122334455\",\"nome\":\"GOKU\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contatos[0].nome", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentArray() {
        Object value = "9988776655";
        String jsonTarget = "{}";
        String jsonResult = "{\"contatos\":[{\"telefone\":\"9988776655\"}]}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.contatos[0].telefone", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoNonExistentArrayMoreComplex() {
        Object value = "JOSE";
        String jsonTarget = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\"}}],\"clientes\":{\"cliente\":{\"tipo-pessoa\":\"PF\"}}}}";
        String jsonResult = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\"}}],\"clientes\":{\"cliente\":{\"tipo-pessoa\":\"PF\",\"contato\":[{\"nome-contato\":\"JOSE\"}]}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.clientes.cliente.contato[0].nome-contato", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArrayMoreComplex() {
        Object value = "MARIA";
        String jsonTarget = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\"}}],\"clientes\":{\"cliente\":{\"tipo-pessoa\":\"PF\",\"contato\":[{\"nome-contato\":\"JOSE\"}]}}}}";
        String jsonResult = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\"}}],\"clientes\":{\"cliente\":{\"tipo-pessoa\":\"PF\",\"contato\":[{\"nome-contato\":\"JOSE\"},{\"nome-contato\":\"MARIA\"}]}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.clientes.cliente.contato[1].nome-contato", value);

        assertThat(result, is(equalTo(jsonResult)));
    }

    @Test
    public void setValueIntoExistentArrayEvenMoreComplex() {
        Object value = "livro";
        String jsonTarget = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\"}}],\"cliente\":{\"endereco\":{\"logradouro\":\"AVENIDA\"}}}}";
        String jsonResult = "{\"pedido\":{\"item-pedido\":[{\"produto\":{\"id\":\"112233\",\"grupo\":{\"tipo\":\"livro\"}}}],\"cliente\":{\"endereco\":{\"logradouro\":\"AVENIDA\"}}}}";

        String result = jsonProcess.setValueIntoJsonPath(jsonTarget, "$.pedido.item-pedido[0].produto.grupo.tipo", value);

        assertThat(result, is(equalTo(jsonResult)));
    }
}