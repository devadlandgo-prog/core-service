package com.landgo.coreservice.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * Sanitizes admin-authored legal document HTML before it is persisted.
 *
 * <p>Legal pages are rendered on LandGo Web with {@code dangerouslySetInnerHTML}, so the stored
 * markup is treated as trusted by the client. Cleaning it here — rather than relying only on the
 * browser-side DOMPurify pass — keeps a compromised or careless admin account from persisting
 * script payloads that every visitor would then load.
 */
public final class LegalHtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            // Layout wrappers and utility classes used by the existing bundled documents.
            .addTags("section", "article", "header", "footer", "hr", "span")
            .addAttributes(":all", "class", "id", "style")
            .addAttributes("a", "target", "rel")
            // Anchors are allowed to be absolute links or in-page fragments.
            .addProtocols("a", "href", "http", "https", "mailto", "#")
            .addProtocols("img", "src", "http", "https", "data");

    private LegalHtmlSanitizer() {
    }

    /**
     * Strips scripts, event handlers and unsafe URL protocols while preserving the document
     * structure and styling classes the web app relies on.
     *
     * @param html raw HTML submitted by an administrator; may be {@code null}
     * @return cleaned HTML, or {@code null} when {@code html} was {@code null}
     */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings()
                .prettyPrint(false);

        return Jsoup.clean(html, "", SAFELIST, outputSettings);
    }
}
