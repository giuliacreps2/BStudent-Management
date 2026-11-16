package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Formato a blocchi unico per lezioni, Risorse e Blog (blueprint §7.4), uguale ai tipi del frontend
 * ({@code types/article.ts}: ArticleBlock; {@code types/resourceDetail.ts}: contenuto delle Risorse).
 * Valida la forma e rifiuta l'HTML: nei testi sono ammessi solo **grassetto** e *corsivo*.
 * Tutti gli errori trovati vengono restituiti insieme, con la posizione ("Blocco 3: il paragrafo da compilare").
 * TODO (4.4): collegare la validazione dei blocchi alle pagine di testo della lezione (LessonPageDTO),
 * quando l'editor a blocchi dell'Admin sarà pronto. Il blocco "taglio" arriverà con il Blog.
 */
public final class ContentBlocks {

    public static final int MAX_BLOCKS = 200;
    public static final int MAX_TEXT = 5000;
    public static final int MAX_SHORT_TEXT = 300;
    public static final int MAX_REFERENCE = 500;
    private static final int MAX_ERRORS_SHOWN = 10;

    // Campi ammessi per tipo di blocco ("type" e "id" sempre presenti)
    private static final Map<String, Set<String>> BLOCK_FIELDS = Map.of(
            "heading", Set.of("text", "number"),
            "paragraph", Set.of("text"),
            "quote", Set.of("text", "author"),
            "callout", Set.of("text", "icon"),
            "image", Set.of("src", "alt", "caption"),
            "list", Set.of("items", "ordered"));

    private static final Set<String> RESOURCE_SECTIONS = Set.of("summaryExample", "tool", "insights", "examples");

    private ContentBlocks() {
    }

    // Elenco di blocchi (corpo di una pagina, di un articolo, di un approfondimento)
    public static void validateBlocks(JsonNode blocks) {
        Errors errors = new Errors();
        checkBlocks(blocks, "", errors);
        errors.throwIfAny();
    }

    // Contenuto di una Risorsa: ogni sezione è facoltativa
    public static void validateResourceContent(JsonNode content) {
        Errors errors = new Errors();
        if (!content.isObject()) {
            throw new BadRequestException("Il contenuto della risorsa non è nel formato atteso");
        }
        for (String name : content.propertyNames()) {
            if (!RESOURCE_SECTIONS.contains(name)) {
                errors.add("sezione sconosciuta: " + name);
            }
        }
        JsonNode example = content.get("summaryExample");
        if (present(example)) {
            checkObject(example, "In breve, esempio", Set.of("term", "translation"), errors);
            requiredText(example, "term", "In breve, esempio: il termine", MAX_SHORT_TEXT, errors);
            requiredText(example, "translation", "In breve, esempio: la traduzione", MAX_SHORT_TEXT, errors);
        }
        JsonNode tool = content.get("tool");
        if (present(tool)) {
            checkTable(tool, errors);
        }
        JsonNode insights = content.get("insights");
        if (present(insights)) {
            checkInsights(insights, errors);
        }
        JsonNode examples = content.get("examples");
        if (present(examples)) {
            checkExamples(examples, errors);
        }
        errors.throwIfAny();
    }

    // Almeno una sezione con contenuto (tabella, approfondimenti o esempi): serve per pubblicare
    public static boolean hasResourceSections(JsonNode content) {
        return present(content.get("tool"))
                || (present(content.get("insights")) && !content.get("insights").isEmpty())
                || (present(content.get("examples")) && !content.get("examples").isEmpty());
    }

    //BLOCCHI

    private static void checkBlocks(JsonNode blocks, String prefix, Errors errors) {
        if (!blocks.isArray()) {
            errors.add(prefix + "i blocchi devono essere un elenco");
            return;
        }
        if (blocks.size() > MAX_BLOCKS) {
            errors.add(prefix + "troppi blocchi (massimo " + MAX_BLOCKS + ")");
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < blocks.size(); i++) {
            String where = prefix + "Blocco " + (i + 1);
            JsonNode block = blocks.get(i);
            if (!block.isObject()) {
                errors.add(where + ": formato non valido");
                continue;
            }
            uniqueId(block, where, ids, errors);
            JsonNode type = block.get("type");
            Set<String> fields = type != null && type.isString() ? BLOCK_FIELDS.get(type.stringValue()) : null;
            if (fields == null) {
                errors.add(where + ": tipo di blocco sconosciuto");
                continue;
            }
            Set<String> allowed = new HashSet<>(fields);
            allowed.add("type");
            allowed.add("id");
            checkObject(block, where, allowed, errors);
            switch (type.stringValue()) {
                case "heading" -> {
                    requiredText(block, "text", where + ": il titolo", MAX_SHORT_TEXT, errors);
                    optionalText(block, "number", where + ": il numero", 10, errors);
                }
                case "paragraph" -> requiredText(block, "text", where + ": il paragrafo", MAX_TEXT, errors);
                case "quote" -> {
                    requiredText(block, "text", where + ": la citazione", MAX_TEXT, errors);
                    optionalText(block, "author", where + ": l'autore", MAX_SHORT_TEXT, errors);
                }
                case "callout" -> {
                    requiredText(block, "text", where + ": il riquadro", MAX_TEXT, errors);
                    optionalText(block, "icon", where + ": l'icona", 8, errors);
                }
                case "image" -> {
                    requiredText(block, "src", where + ": l'immagine", MAX_REFERENCE, errors);
                    requiredText(block, "alt", where + ": la descrizione dell'immagine", MAX_SHORT_TEXT, errors);
                    optionalText(block, "caption", where + ": la didascalia", MAX_SHORT_TEXT, errors);
                }
                case "list" -> {
                    checkStringList(block.get("items"), where + ": l'elenco", MAX_TEXT, errors);
                    optionalBoolean(block, "ordered", where, errors);
                }
                default -> errors.add(where + ": tipo di blocco sconosciuto");
            }
        }
    }

    //RISORSE

    private static void checkTable(JsonNode tool, Errors errors) {
        String where = "Tabella";
        if (!tool.isObject()) {
            errors.add(where + ": formato non valido");
            return;
        }
        checkObject(tool, where, Set.of("type", "title", "variants"), errors);
        JsonNode type = tool.get("type");
        if (type == null || !type.isString() || !"table".equals(type.stringValue())) {
            errors.add(where + ": oggi è ammessa solo la tabella");
        }
        requiredText(tool, "title", where + ": il titolo", MAX_SHORT_TEXT, errors);
        JsonNode variants = tool.get("variants");
        if (variants == null || !variants.isArray() || variants.isEmpty()) {
            errors.add(where + ": serve almeno una variante");
            return;
        }
        if (variants.size() > 10) {
            errors.add(where + ": troppe varianti (massimo 10)");
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < variants.size(); i++) {
            String variantWhere = where + ", variante " + (i + 1);
            JsonNode variant = variants.get(i);
            if (!variant.isObject()) {
                errors.add(variantWhere + ": formato non valido");
                continue;
            }
            checkObject(variant, variantWhere, Set.of("id", "label", "columns", "rows", "note"), errors);
            uniqueId(variant, variantWhere, ids, errors);
            requiredText(variant, "label", variantWhere + ": il nome", MAX_SHORT_TEXT, errors);
            optionalText(variant, "note", variantWhere + ": la nota", MAX_TEXT, errors);
            JsonNode columns = variant.get("columns");
            checkStringList(columns, variantWhere + ": le colonne", MAX_SHORT_TEXT, errors);
            int columnCount = columns != null && columns.isArray() ? columns.size() : 0;
            if (columnCount > 8) {
                errors.add(variantWhere + ": troppe colonne (massimo 8)");
            }
            JsonNode rows = variant.get("rows");
            if (rows == null || !rows.isArray() || rows.isEmpty()) {
                errors.add(variantWhere + ": serve almeno una riga");
                continue;
            }
            if (rows.size() > 100) {
                errors.add(variantWhere + ": troppe righe (massimo 100)");
            }
            for (int r = 0; r < rows.size(); r++) {
                JsonNode row = rows.get(r);
                String rowWhere = variantWhere + ", riga " + (r + 1);
                if (!row.isArray() || row.size() != columnCount) {
                    errors.add(rowWhere + ": servono tanti valori quante colonne (" + columnCount + ")");
                    continue;
                }
                for (JsonNode cell : row) {
                    if (!cell.isString() || cell.stringValue().length() > MAX_SHORT_TEXT || hasHtml(cell.stringValue())) {
                        errors.add(rowWhere + ": valore non valido");
                        break;
                    }
                }
            }
        }
    }

    private static void checkInsights(JsonNode insights, Errors errors) {
        if (!insights.isArray()) {
            errors.add("Approfondimenti: devono essere un elenco");
            return;
        }
        if (insights.size() > 30) {
            errors.add("Approfondimenti: troppi (massimo 30)");
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < insights.size(); i++) {
            String where = "Approfondimento " + (i + 1);
            JsonNode insight = insights.get(i);
            if (!insight.isObject()) {
                errors.add(where + ": formato non valido");
                continue;
            }
            checkObject(insight, where, Set.of("id", "title", "defaultOpen", "blocks"), errors);
            uniqueId(insight, where, ids, errors);
            requiredText(insight, "title", where + ": il titolo", MAX_SHORT_TEXT, errors);
            optionalBoolean(insight, "defaultOpen", where, errors);
            JsonNode blocks = insight.get("blocks");
            if (blocks == null || !blocks.isArray() || blocks.isEmpty()) {
                errors.add(where + ": serve almeno un blocco");
            } else {
                checkBlocks(blocks, where + ", ", errors);
            }
        }
    }

    private static void checkExamples(JsonNode examples, Errors errors) {
        if (!examples.isArray()) {
            errors.add("Esempi: devono essere un elenco");
            return;
        }
        if (examples.size() > 50) {
            errors.add("Esempi: troppi (massimo 50)");
        }
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < examples.size(); i++) {
            String where = "Esempio " + (i + 1);
            JsonNode example = examples.get(i);
            if (!example.isObject()) {
                errors.add(where + ": formato non valido");
                continue;
            }
            checkObject(example, where, Set.of("id", "term", "grammar", "sentence", "translation"), errors);
            uniqueId(example, where, ids, errors);
            requiredText(example, "term", where + ": il termine", MAX_SHORT_TEXT, errors);
            optionalText(example, "grammar", where + ": l'indicazione grammaticale", MAX_SHORT_TEXT, errors);
            requiredText(example, "sentence", where + ": la frase", MAX_TEXT, errors);
            requiredText(example, "translation", where + ": la traduzione", MAX_TEXT, errors);
        }
    }

    //SUPPORTO

    private static boolean present(JsonNode node) {
        return node != null && !node.isNull() && !node.isMissingNode();
    }

    private static void checkObject(JsonNode node, String where, Set<String> allowed, Errors errors) {
        if (!node.isObject()) {
            errors.add(where + ": formato non valido");
            return;
        }
        for (String name : node.propertyNames()) {
            if (!allowed.contains(name)) {
                errors.add(where + ": campo non previsto (" + name + ")");
            }
        }
    }

    private static void uniqueId(JsonNode node, String where, Set<String> ids, Errors errors) {
        JsonNode id = node.get("id");
        if (id == null || !id.isString() || id.stringValue().isBlank() || id.stringValue().length() > 60) {
            errors.add(where + ": manca l'identificativo");
        } else if (!ids.add(id.stringValue())) {
            errors.add(where + ": identificativo ripetuto (" + id.stringValue() + ")");
        }
    }

    private static void requiredText(JsonNode node, String field, String label, int max, Errors errors) {
        JsonNode value = node.get(field);
        if (value == null || !value.isString() || value.stringValue().isBlank()) {
            errors.add(label + " da compilare");
        } else {
            checkText(value.stringValue(), label, max, errors);
        }
    }

    private static void optionalText(JsonNode node, String field, String label, int max, Errors errors) {
        JsonNode value = node.get(field);
        if (!present(value)) {
            return;
        }
        if (!value.isString()) {
            errors.add(label + ": deve essere un testo");
        } else {
            checkText(value.stringValue(), label, max, errors);
        }
    }

    private static void checkText(String text, String label, int max, Errors errors) {
        if (text.length() > max) {
            errors.add(label + " supera i " + max + " caratteri");
        }
        if (hasHtml(text)) {
            errors.add(label + " contiene i caratteri < o >: si usano solo **grassetto** e *corsivo*");
        }
    }

    private static void optionalBoolean(JsonNode node, String field, String where, Errors errors) {
        JsonNode value = node.get(field);
        if (present(value) && !value.isBoolean()) {
            errors.add(where + ": " + field + " deve essere vero o falso");
        }
    }

    private static void checkStringList(JsonNode list, String label, int max, Errors errors) {
        if (list == null || !list.isArray() || list.isEmpty()) {
            errors.add(label + " da compilare");
            return;
        }
        for (JsonNode item : list) {
            if (!item.isString() || item.stringValue().isBlank()) {
                errors.add(label + " contiene una voce vuota");
                return;
            }
            checkText(item.stringValue(), label, max, errors);
        }
    }

    // Niente HTML: il frontend mostra il testo, non lo interpreta
    private static boolean hasHtml(String text) {
        return text.indexOf('<') >= 0 || text.indexOf('>') >= 0;
    }

    private static final class Errors {
        private final List<String> messages = new ArrayList<>();

        void add(String message) {
            messages.add(message);
        }

        void throwIfAny() {
            if (messages.isEmpty()) {
                return;
            }
            List<String> shown = messages.size() > MAX_ERRORS_SHOWN ? messages.subList(0, MAX_ERRORS_SHOWN) : messages;
            String more = messages.size() > MAX_ERRORS_SHOWN ? " (e altri " + (messages.size() - MAX_ERRORS_SHOWN) + ")" : "";
            throw new BadRequestException("Il contenuto non è valido: " + String.join("; ", shown) + more, messages);
        }
    }
}
