package app.financeiro.pessoal;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Identifica se a notificação é uma movimentação e extrai tipo, valor e nome. */
public class Parser {
    public static class R { public String tipo, n; public double v; }

    static final Pattern VAL = Pattern.compile("R\\$\\s?(\\d{1,3}(?:\\.\\d{3})*,\\d{2}|\\d+,\\d{2})");
    static final Pattern NAME1 = Pattern.compile("^(.{2,40}?)\\s+te\\s+(?:enviou|transferiu|fez|mandou)");
    static final Pattern NAME2 = Pattern.compile("\\b(?:em|no|na|para|pra|de|do|da|com|por)\\s+([\\p{Lu}\\d][^.,;!?\\n]{1,40})");

    // Palavras que indicam ENTRADA de dinheiro (sem acento, minúsculas)
    static final String[] IN = {"te enviou", "enviou para voce", "enviou pra voce", "enviou a voce", "voce recebeu",
        "pix recebido", "transferencia recebida", "recebida", "recebido", "recebemos", "deposito",
        "credito em conta", "estorno", "reembolso", "salario"};
    // Palavras que indicam SAÍDA de dinheiro
    static final String[] OUT = {"voce pagou", "pagamento", "pagamos", "compra", "debito", "pix enviado",
        "pix realizado", "voce enviou", "voce fez um pix", "voce transferiu", "transferencia enviada",
        "transferencia realizada", "enviou um pix", "saque", "boleto pago", "debitado"};
    // Mensagens que NÃO são movimentação (propaganda, fatura a vencer, códigos...)
    static final String[] SKIP = {"codigo", "senha", "token", "oferta", "promocao", "limite disponivel",
        "fatura fechou", "fatura disponivel", "vence", "vencimento", "simule", "convite", "cashback",
        "rendimento", "rendeu", "agendad", "programad", "lembrete", "pre-aprovado"};

    public static String norm(String s) {
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    static boolean has(String n, String[] a) {
        for (String s : a) if (n.contains(s)) return true;
        return false;
    }

    public static R parse(String title, String text) {
        String all = title + " " + text, n = norm(all);
        if (has(n, SKIP)) return null;
        Matcher m = VAL.matcher(all);
        if (!m.find()) return null;
        double v;
        try { v = Double.parseDouble(m.group(1).replace(".", "").replace(",", ".")); } catch (Exception e) { return null; }
        if (v <= 0) return null;
        String tipo = has(n, IN) ? "r" : has(n, OUT) ? "d" : null;
        if (tipo == null) return null;
        String body = VAL.matcher(text).replaceAll("").replaceAll("\\s+", " ").trim();
        String name = null;
        Matcher a = NAME1.matcher(body);
        if (a.find()) name = a.group(1);
        else {
            Matcher b = NAME2.matcher(body);
            while (b.find()) {
                String g = b.group(1);
                if (!norm(g).matches("^(aprovad|realizad|efetuad|confirmad|recebid|enviad).*")) { name = g; break; }
                Matcher c = NAME2.matcher(g);
                if (c.find()) { name = c.group(1); break; }
            }
        }
        if (name == null) name = has(norm(title), IN) || has(norm(title), OUT) || body.length() < 6 ? title : body;
        name = name.split("\\s+(?:\\u00e0s|as|via|no valor|em|no|na|por|com|dia)\\s+")[0].trim();
        if (name.length() > 60) name = name.substring(0, 60).trim();
        if (name.isEmpty()) name = title;
        R r = new R(); r.tipo = tipo; r.v = v; r.n = name;
        return r;
    }
}
