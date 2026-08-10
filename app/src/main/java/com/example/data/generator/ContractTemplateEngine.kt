package com.example.data.generator

import com.example.data.model.ContractType
import com.example.data.model.FieldType
import com.example.data.model.FormField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ContractTemplateEngine {

    fun getDefaultFieldsForType(type: ContractType): List<FormField> {
        val currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
        return when (type) {
            ContractType.SOFTWARE_DEV -> listOf(
                FormField("effective_date", "Effective Date", currentDate, FieldType.DATE, currentDate),
                FormField("jurisdiction", "Governing Law / Jurisdiction", "US - California Law (US ESIGN Compliant)", FieldType.DROPDOWN, "US - California Law (US ESIGN Compliant)", options = listOf(
                    "US - California Law (US ESIGN Compliant)",
                    "US - Delaware Corporate Law",
                    "US - New York Law",
                    "EU - Germany / GDPR Compliant",
                    "EU - France / EU eIDAS Standard",
                    "United Kingdom (English Law)",
                    "International / UNCITRAL Arbitration"
                )),
                FormField("client_name", "Client / Company Name", "Acme Corporation Inc."),
                FormField("client_tax_id", "Client Tax ID / EIN / VAT No.", "EIN 12-3456789"),
                FormField("client_email", "Client Email Address", "client@acme.com"),
                FormField("dev_name", "Developer / Contractor Name", "Alex Rivera (Apex Software LLC)"),
                FormField("dev_tax_id", "Developer SSN / EIN / Tax ID", "EIN 98-7654321"),
                FormField("dev_email", "Developer Email Address", "alex@apexsoftware.dev"),
                FormField("project_scope", "Scope of Work & Technical Specs", "Design and development of iOS/Android Mobile App with REST API backend, Stripe payment gateway integration, and admin portal.", FieldType.MULTILINE),
                FormField("total_fee", "Total Project Fee", "$12,500 USD", FieldType.CURRENCY),
                FormField("payment_terms", "Payment Schedule & Milestones", "50% ($6,250) Deposit upon signing, 25% upon Beta deliverable, 25% upon final App Store deployment.", FieldType.MULTILINE),
                FormField("completion_date", "Target Completion Date", "11/30/2026", FieldType.DATE),
                FormField("ip_clause", "Intellectual Property Ownership", "Full Transfer of Source Code & IP Ownership to Client upon final payment.", FieldType.DROPDOWN, "Full Transfer of Source Code & IP Ownership to Client upon final payment.", options = listOf(
                    "Full Transfer of Source Code & IP Ownership to Client upon final payment.",
                    "Non-exclusive perpetual license to Client; Developer retains core framework IP.",
                    "Exclusive commercial license restricted to specified platform/territory."
                )),
                FormField("special_terms", "Warranty & Support Terms (Optional)", "Includes 30 days of free post-launch bug fixes and maintenance.", FieldType.MULTILINE, isRequired = false)
            )

            ContractType.GRAPHIC_DESIGN -> listOf(
                FormField("effective_date", "Effective Date", currentDate, FieldType.DATE, currentDate),
                FormField("jurisdiction", "Governing Law / Jurisdiction", "US - New York Law (US ESIGN Compliant)", FieldType.DROPDOWN, "US - New York Law (US ESIGN Compliant)", options = listOf(
                    "US - New York Law (US ESIGN Compliant)",
                    "US - California Law",
                    "US - Delaware Corporate Law",
                    "EU - Germany / GDPR Compliant",
                    "EU - France / EU eIDAS Standard",
                    "United Kingdom (English Law)",
                    "International / UNCITRAL Arbitration"
                )),
                FormField("client_name", "Client / Brand Name", "Vanguard Brand Studio Ltd."),
                FormField("client_tax_id", "Client Tax ID / EIN / VAT No.", "VAT GB 123 4567 89"),
                FormField("client_email", "Client Email Address", "hello@vanguard.com"),
                FormField("designer_name", "Designer / Agency Name", "Sophia Chen Design Studio"),
                FormField("designer_tax_id", "Designer SSN / EIN / Tax ID", "EIN 45-6789123"),
                FormField("designer_email", "Designer Email Address", "sophia@chendesign.co"),
                FormField("design_deliverables", "Deliverable Assets & Guidelines", "Complete Brand Identity system: Vector Logo Suite (AI, SVG, PNG), Color Palette System, Typography Rules, & 20-page Brand Guidelines PDF.", FieldType.MULTILINE),
                FormField("total_fee", "Design Project Fee", "$4,800 USD", FieldType.CURRENCY),
                FormField("payment_terms", "Payment Terms", "50% upfront deposit required before project commencement; balance due upon final vector asset delivery.", FieldType.MULTILINE),
                FormField("revision_limit", "Included Revision Rounds", "Up to 3 rounds of revisions included. Additional revisions billed at $120/hour."),
                FormField("special_terms", "Portfolio / Promotion Rights (Optional)", "Designer reserves the right to display completed work in online portfolio.", FieldType.MULTILINE, isRequired = false)
            )

            ContractType.SOCIAL_MEDIA -> listOf(
                FormField("effective_date", "Effective Date", currentDate, FieldType.DATE, currentDate),
                FormField("jurisdiction", "Governing Law / Jurisdiction", "EU - GDPR & eIDAS Compliant Standard", FieldType.DROPDOWN, "EU - GDPR & eIDAS Compliant Standard", options = listOf(
                    "EU - GDPR & eIDAS Compliant Standard",
                    "US - Delaware Corporate Law",
                    "US - California Law",
                    "US - New York Law",
                    "United Kingdom (English Law)",
                    "International / UNCITRAL Arbitration"
                )),
                FormField("client_name", "Client Company / Creator Name", "Lumina Skincare Corp."),
                FormField("client_tax_id", "Client Tax ID / EIN / VAT No.", "VAT DE 987654321"),
                FormField("client_email", "Client Email Address", "marketing@luminaskincare.com"),
                FormField("agency_name", "Agency / Strategist Name", "SocialPulse Growth Agency LLC"),
                FormField("agency_tax_id", "Agency Tax ID / EIN", "EIN 77-8899001"),
                FormField("agency_email", "Agency Email Address", "accounts@socialpulse.agency"),
                FormField("managed_platforms", "Target Platforms & Frequency", "Instagram, TikTok & LinkedIn: 16 Custom Short-Form Videos/Reels per month + 20 Static Posts + Daily Community Management.", FieldType.MULTILINE),
                FormField("monthly_retainer", "Monthly Retainer Fee", "$3,500 USD / month", FieldType.CURRENCY),
                FormField("contract_duration", "Agreement Term", "6-Month Initial Term with Month-to-Month renewal thereafter."),
                FormField("ad_budget_policy", "Ad Spend & Ad Account Ownership", "Third-party ad spend billed directly to Client's ad account. Agency acts as manager."),
                FormField("special_terms", "Notice Period for Termination", "30 days written notice prior to next monthly billing cycle.", FieldType.MULTILINE, isRequired = false)
            )
        }
    }

    fun generateDraftText(type: ContractType, values: Map<String, String>): String {
        val date = values["effective_date"] ?: SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
        val jurisdiction = values["jurisdiction"] ?: "US - Federal & State Law (ESIGN Compliant)"

        return when (type) {
            ContractType.SOFTWARE_DEV -> {
                val client = values["client_name"] ?: "CLIENT"
                val clientTax = values["client_tax_id"] ?: "-"
                val dev = values["dev_name"] ?: "DEVELOPER"
                val devTax = values["dev_tax_id"] ?: "-"
                val scope = values["project_scope"] ?: "Software engineering services."
                val fee = values["total_fee"] ?: "$0 USD"
                val terms = values["payment_terms"] ?: "Net 15"
                val targetDate = values["completion_date"] ?: "TBD"
                val ipTransfer = values["ip_clause"] ?: "Full IP transfer upon final payment."
                val special = values["special_terms"] ?: "Standard 30-day warranty."

                """
                ============================================================
                          SOFTWARE DEVELOPMENT AGREEMENT
                ============================================================
                Effective Date: $date
                Reference ID: CG-DEV-${System.currentTimeMillis() % 100000}
                Governing Law: $jurisdiction

                1. PARTIES
                ------------------------------------------------------------
                CLIENT: $client
                Tax ID / Reg No: $clientTax

                DEVELOPER: $dev
                Tax ID / Reg No: $devTax

                2. SCOPE OF WORK & SERVICES
                ------------------------------------------------------------
                Developer agrees to perform professional software engineering services for Client as detailed below:

                $scope

                • Target Completion Date: $targetDate
                • Delivery Standard: Work shall be delivered bug-free according to technical specifications and tested across supported platforms.

                3. COMPENSATION & PAYMENT TERMS
                ------------------------------------------------------------
                • Total Fee: $fee
                • Payment Schedule: $terms
                • Late Payments: Invoices overdue by more than 15 calendar days shall accrue interest at 1.5% per month or the maximum rate allowed by law.

                4. INTELLECTUAL PROPERTY RIGHTS
                ------------------------------------------------------------
                $ipTransfer
                Developer warrants that all work product produced hereunder is original and does not infringe upon any third-party patents, copyrights, or trade secrets.

                5. CONFIDENTIALITY & NON-DISCLOSURE
                ------------------------------------------------------------
                Both Parties agree to maintain strict confidentiality regarding all proprietary business data, codebases, API credentials, and trade secrets disclosed during the course of performance.

                6. WARRANTY & SUPPORT
                ------------------------------------------------------------
                $special

                7. E-SIGNATURE LEGAL VALIDITY & JURISDICTION
                ------------------------------------------------------------
                This Agreement is executed electronically in compliance with the US ESIGN Act (15 U.S.C. § 7001 et seq.), the Uniform Electronic Transactions Act (UETA), and EU eIDAS Regulation (No 910/2014). Both Parties agree that digital signatures affixed hereto constitute legal and binding assent.

                IN WITNESS WHEREOF, the Parties have executed this Agreement electronically on the Effective Date set forth above.
                """.trimIndent()
            }

            ContractType.GRAPHIC_DESIGN -> {
                val client = values["client_name"] ?: "CLIENT"
                val clientTax = values["client_tax_id"] ?: "-"
                val designer = values["designer_name"] ?: "DESIGNER"
                val designerTax = values["designer_tax_id"] ?: "-"
                val deliverables = values["design_deliverables"] ?: "Graphic design services."
                val fee = values["total_fee"] ?: "$0 USD"
                val terms = values["payment_terms"] ?: "50% deposit, 50% on delivery."
                val revisions = values["revision_limit"] ?: "3 Revision rounds."
                val special = values["special_terms"] ?: "Portfolio rights reserved."

                """
                ============================================================
                        GRAPHIC & LOGO DESIGN CONTRACT
                ============================================================
                Effective Date: $date
                Reference ID: CG-DSGN-${System.currentTimeMillis() % 100000}
                Governing Law: $jurisdiction

                1. PARTIES
                ------------------------------------------------------------
                CLIENT: $client
                Tax ID / Reg No: $clientTax

                DESIGNER: $designer
                Tax ID / Reg No: $designerTax

                2. DESIGN DELIVERABLES & SERVICES
                ------------------------------------------------------------
                Designer agrees to create and deliver vector and raster graphic assets according to the following scope:

                $deliverables

                3. FEES & PAYMENT SCHEDULE
                ------------------------------------------------------------
                • Total Design Fee: $fee
                • Payment Terms: $terms
                • Revision Policy: $revisions

                4. INTELLECTUAL PROPERTY & COPYRIGHT ASSIGNMENT
                ------------------------------------------------------------
                Upon receipt of full payment, Designer assigns to Client all exclusive, worldwide rights, title, and copyright in the final chosen design deliverables. Preliminary concepts and rejected drafts remain the sole property of Designer.

                5. PROMOTIONAL RIGHTS
                ------------------------------------------------------------
                $special

                6. ELECTRONIC ASSENT & BINDING NATURE
                ------------------------------------------------------------
                This Agreement is validly signed via cryptographic digital signature under US ESIGN Act and EU eIDAS regulations.

                IN WITNESS WHEREOF, the Parties accept and sign this Contract electronically.
                """.trimIndent()
            }

            ContractType.SOCIAL_MEDIA -> {
                val client = values["client_name"] ?: "CLIENT"
                val clientTax = values["client_tax_id"] ?: "-"
                val agency = values["agency_name"] ?: "AGENCY"
                val agencyTax = values["agency_tax_id"] ?: "-"
                val platforms = values["managed_platforms"] ?: "Social media management."
                val retainer = values["monthly_retainer"] ?: "$0 / month"
                val duration = values["contract_duration"] ?: "6 Months"
                val adSpend = values["ad_budget_policy"] ?: "Client pays ad spend directly."
                val special = values["special_terms"] ?: "30 days cancellation notice."

                """
                ============================================================
                     SOCIAL MEDIA MANAGEMENT AGREEMENT
                ============================================================
                Effective Date: $date
                Reference ID: CG-SMM-${System.currentTimeMillis() % 100000}
                Governing Law: $jurisdiction

                1. PARTIES
                ------------------------------------------------------------
                CLIENT: $client
                Tax ID / Reg No: $clientTax

                AGENCY / STRATEGIST: $agency
                Tax ID / Reg No: $agencyTax

                2. MANAGED CHANNELS & CONTENT SCOPE
                ------------------------------------------------------------
                Agency shall provide social media strategy, content creation, scheduling, and community engagement for the following channels:

                $platforms

                3. RETAINER FEE & BILLING CYCLE
                ------------------------------------------------------------
                • Monthly Retainer Fee: $retainer
                • Agreement Duration: $duration
                • Advertising Budget Policy: $adSpend

                4. CONTENT APPROVAL & BRAND GUIDELINES
                ------------------------------------------------------------
                Client shall review and approve content calendars within 48 hours of submission. Agency warrants that all published materials comply with platform guidelines and applicable GDPR advertising privacy rules.

                5. TERMINATION & CANCELLATION
                ------------------------------------------------------------
                $special

                6. DIGITAL SIGNATURE COMPLIANCE
                ------------------------------------------------------------
                Executed electronically in accordance with US ESIGN Act and EU eIDAS Standards.

                IN WITNESS WHEREOF, the Parties execute this Social Media Management Agreement.
                """.trimIndent()
            }
        }
    }
}

