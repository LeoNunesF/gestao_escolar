package com.gestaoescolar.views.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestaoescolar.model.Endereco;
import com.gestaoescolar.model.Estado;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * EnderecoForm reutilizável:
 * - expõe getters para fields (úteis para Binder externos)
 * - consulta ViaCEP no blur do CEP e preenche logradouro/bairro/cidade/estado (convertendo para Estado enum)
 */
public class EnderecoForm extends FormLayout {

    private static final Pattern CEP_PATTERN = Pattern.compile("\\d{5}-?\\d{3}");

    private final TextField cep = new TextField("CEP");
    private final TextField logradouro = new TextField("Logradouro");
    private final TextField numero = new TextField("Número");
    private final TextField complemento = new TextField("Complemento");
    private final TextField bairro = new TextField("Bairro");
    private final TextField cidade = new TextField("Cidade");
    private final ComboBox<Estado> estado = new ComboBox<>("Estado");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EnderecoForm() {
        cep.setMaxLength(9);
        estado.setItems(Estado.values());
        estado.setItemLabelGenerator(Estado::getSigla);
        estado.setWidth("8em");

        // formata cep no blur e tenta consulta ViaCEP
        cep.addBlurListener(e -> {
            String raw = cep.getValue();
            if (raw == null || raw.isBlank()) return;
            String formatted = formatCep(raw);
            cep.setValue(formatted);

            String digits = onlyDigits(formatted);
            if (digits.length() == 8) {
                consultarViaCepAsync(digits);
            }
        });

        add(cep, logradouro, numero, complemento, bairro, cidade, estado);
    }

    // Getters para binder externo
    public TextField getCepField() { return cep; }
    public TextField getLogradouroField() { return logradouro; }
    public TextField getNumeroField() { return numero; }
    public TextField getComplementoField() { return complemento; }
    public TextField getBairroField() { return bairro; }
    public TextField getCidadeField() { return cidade; }
    public ComboBox<Estado> getEstadoField() { return estado; }

    public Endereco getEndereco() {
        Endereco e = new Endereco();
        e.setCep(cep.getValue());
        e.setLogradouro(logradouro.getValue());
        e.setNumero(numero.getValue());
        e.setComplemento(complemento.getValue());
        e.setBairro(bairro.getValue());
        e.setCidade(cidade.getValue());
        e.setEstado(estado.getValue());
        return e;
    }

    public void setEndereco(Endereco e) {
        if (e == null) {
            cep.clear(); logradouro.clear(); numero.clear(); complemento.clear();
            bairro.clear(); cidade.clear(); estado.clear();
            return;
        }
        cep.setValue(e.getCep() == null ? "" : e.getCep());
        logradouro.setValue(e.getLogradouro() == null ? "" : e.getLogradouro());
        numero.setValue(e.getNumero() == null ? "" : e.getNumero());
        complemento.setValue(e.getComplemento() == null ? "" : e.getComplemento());
        bairro.setValue(e.getBairro() == null ? "" : e.getBairro());
        cidade.setValue(e.getCidade() == null ? "" : e.getCidade());
        estado.setValue(e.getEstado());
    }

    // Helpers
    private String onlyDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    private String formatCep(String input) {
        String digits = onlyDigits(input);
        if (digits.length() == 8) {
            return String.format("%s-%s", digits.substring(0,5), digits.substring(5));
        } else {
            return input;
        }
    }

    public boolean isCepValido() {
        String c = cep.getValue();
        return c == null || c.isBlank() || CEP_PATTERN.matcher(c).matches();
    }

    // Consulta ViaCEP de forma assíncrona e atualiza campos no UI thread
    private void consultarViaCepAsync(String cepDigits) {
        String url = "https://viacep.com.br/ws/" + cepDigits + "/json/";
        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return response.body();
                }
            } catch (IOException | InterruptedException ex) {
                // opcional: log
            }
            return null;
        }).thenAccept(json -> {
            if (json == null) return;
            try {
                JsonNode node = objectMapper.readTree(json);
                if (node.has("erro") && node.get("erro").asBoolean()) {
                    UI.getCurrent().access(() ->
                            Notification.show("CEP não encontrado", 3000, Notification.Position.MIDDLE));
                    return;
                }
                String bairroVal = node.hasNonNull("bairro") ? node.get("bairro").asText() : "";
                String cidadeVal = node.hasNonNull("localidade") ? node.get("localidade").asText() : "";
                String estadoVal = node.hasNonNull("uf") ? node.get("uf").asText() : "";
                String logradouroVal = node.hasNonNull("logradouro") ? node.get("logradouro").asText() : "";

                UI.getCurrent().access(() -> {
                    if (logradouroVal != null && !logradouroVal.isBlank()) logradouro.setValue(logradouroVal);
                    if (bairroVal != null && !bairroVal.isBlank()) bairro.setValue(bairroVal);
                    if (cidadeVal != null && !cidadeVal.isBlank()) cidade.setValue(cidadeVal);
                    if (estadoVal != null && !estadoVal.isBlank()) {
                        Estado e = Estado.fromSigla(estadoVal);
                        if (e != null) estado.setValue(e);
                        else estado.setValue(null);
                    }
                });
            } catch (Exception ex) {
                // parsing error - ignore
            }
        });
    }
}