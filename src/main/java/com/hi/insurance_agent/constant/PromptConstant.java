package com.hi.insurance_agent.constant;

public interface PromptConstant {
    /**
     * 提示词常量，用于存放固定的提示词
     */
    String MARKDOWN_GENERATE_PROMPT = """
            Convert the extracted text from a PDF file into **Markdown format** for knowledge base construction.
            
            Requirements:
            1) Structured Formatting
               - Use Markdown headers (#, ##, ###) to reflect the document hierarchy (chapters, sections, clauses, appendices).
               - Keep logical order and proper nesting.
            
            2) Content Integrity
               - Preserve ALL original information: numbers, tables, definitions, notes, formulas, symbols, clause numbers.
               - Do NOT summarize, rewrite, or omit any content.
            
            3) YAML Front Matter Metadata
               - Insert this block at the very top (I will supply the actual values of title, index, category, and abolition_statement):
                 ---
                 title: {{title}}
                 index: {{index}}
                 category: {{category}}
                 effective_date: {{effective_date}}
                 abolition_statement: {{abolition_statement}}
                 esg: {{esg}}
                 ---
               - Keep the exact '---' delimiters.
               - The field **effective_date** should be automatically detected from the document content if possible (look for terms like “生效日”, “生效日期”, “effective date”, or similar).
               - If no effective date is found, use the default value **2025-01-01**.
               - The format of `effective_date` must always follow `yyyy-mm-dd`. \s
                 - If only the year is present, set it to `yyyy-01-01`. \s
                 - If only the year and month are present, set it to `yyyy-mm-01`.
            
            4) ESG and Regulatory Compliance Detection
               - Automatically detect whether the document includes or aligns with **ESG-related regulatory clauses** or **latest supervisory frameworks**.
               - ESG scope includes but is not limited to:
                 - Environmental: carbon emission, green finance, energy conservation, ecological protection.
                 - Social: labor protection, diversity, equality, consumer rights, data privacy, social responsibility.
                 - Governance: corporate ethics, internal control, board structure, transparency, anti-corruption.
               - Regulatory alignment should reference current Chinese and international frameworks, such as:
                 - 《人身保险监管评级办法》 and related CIRC/CBIRC circulars.
                 - ESG disclosure guidelines issued by the CSRC or CBIRC.
                 - Financial institutions’ sustainability or responsible investment regulations.
               - If the document’s content explicitly or implicitly reflects compliance with such regulatory or ESG principles,
                 set `esg: true`; otherwise set `esg: false`.
               - Do not add commentary; determine logically based on textual evidence.
            
            5) Markdown Style
               - Use standard Markdown only (no HTML).
               - Format tables, lists, quotes, emphasis correctly.
               - Remove page numbers, headers/footers, or meaningless line breaks from the raw extraction.
            
            6) Output
               - Produce a clean, well-structured Markdown document ready for direct import into a knowledge base.
               - Do NOT include extra explanations or reasoning—output the Markdown only.
            """;

}
