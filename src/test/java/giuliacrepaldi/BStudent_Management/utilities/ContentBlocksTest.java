package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentBlocksTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    private JsonNode json(String text) {
        return mapper.readTree(text);
    }

    @Test
    void iBlocchiDelFrontendSonoAccettati() {
        JsonNode blocks = json("""
                [ {"type":"heading","id":"h1","number":"01","text":"L'ablativo assoluto"},
                  {"type":"paragraph","id":"p1","text":"Si forma con **participio** e *nome*."},
                  {"type":"quote","id":"q1","text":"Alea iacta est","author":"Cesare"},
                  {"type":"callout","id":"c1","icon":"💡","text":"Attenzione al soggetto"},
                  {"type":"image","id":"i1","src":"bstudent/abl-assoluto","alt":"Schema"},
                  {"type":"list","id":"l1","items":["primo","secondo"],"ordered":true} ]
                """);
        assertThatCode(() -> ContentBlocks.validateBlocks(blocks)).doesNotThrowAnyException();
    }

    @Test
    void lHtmlNeiTestiVieneRifiutato() {
        JsonNode blocks = json("[ {\"type\":\"paragraph\",\"id\":\"p1\",\"text\":\"<script>x</script>\"} ]");
        assertThatThrownBy(() -> ContentBlocks.validateBlocks(blocks))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Blocco 1");
    }

    @Test
    void tipoSconosciutoEIdRipetutoVengonoSegnalatiInsieme() {
        JsonNode blocks = json("""
                [ {"type":"video","id":"a"}, {"type":"paragraph","id":"b","text":"uno"},
                  {"type":"paragraph","id":"b","text":"due"} ]
                """);
        assertThatThrownBy(() -> ContentBlocks.validateBlocks(blocks))
                .hasMessageContaining("Blocco 1: tipo di blocco sconosciuto")
                .hasMessageContaining("Blocco 3: identificativo ripetuto");
    }

    @Test
    void ogniRigaDellaTabellaHaTantiValoriQuanteColonne() {
        JsonNode content = json("""
                {"tool":{"type":"table","title":"Terza declinazione","variants":[
                  {"id":"v1","label":"Tema consonantico","columns":["Caso","Singolare","Plurale"],
                   "rows":[["Nom.","lex","leges"],["Gen.","legis"]]} ]}}
                """);
        assertThatThrownBy(() -> ContentBlocks.validateResourceContent(content))
                .hasMessageContaining("riga 2");
    }

    @Test
    void unaRisorsaCompletaEValidaEHaSezioni() {
        JsonNode content = json("""
                {"summaryExample":{"term":"lex, legis","translation":"legge"},
                 "insights":[{"id":"in1","title":"Perché","blocks":[{"type":"paragraph","id":"p","text":"Testo"}]}],
                 "examples":[{"id":"e1","term":"lex","grammar":"(f.)","sentence":"Dura lex","translation":"Legge dura"}]}
                """);
        assertThatCode(() -> ContentBlocks.validateResourceContent(content)).doesNotThrowAnyException();
        assertThat(ContentBlocks.hasResourceSections(content)).isTrue();
        assertThat(ContentBlocks.hasResourceSections(json("{\"summaryExample\":{\"term\":\"a\",\"translation\":\"b\"}}"))).isFalse();
    }

    @Test
    void sezioniSconosciuteVengonoRifiutate() {
        assertThatThrownBy(() -> ContentBlocks.validateResourceContent(json("{\"related\":[]}")))
                .hasMessageContaining("sezione sconosciuta: related");
    }
}
