package com.backend.givr.organization.service.verify;

import com.backend.givr.shared.enums.ReviewStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.text.Normalizer;

@Service
public class OrganizationVerifier {

    private final LevenshteinDistance levenshteinDistance;

    // Configurable thresholds (adjust based on your data)
    private static final double NAME_SIMILARITY_THRESHOLD = 85.0;
    private static final double ADDRESS_SIMILARITY_THRESHOLD = 60.0;
    private static final int MAX_LEVENSHTEIN_DISTANCE = Integer.MAX_VALUE; // No limit

    // Data Classes
    @Setter
    @Getter
    public static class NameComparisonResult {
        private boolean match;
        private double similarity;
        private String reason;
        // Getters and setters
        public boolean getMatch(){
            return match;
        }
    }

    public OrganizationVerifier() {
        // Initialize with an unlimited distance threshold
        this.levenshteinDistance = new LevenshteinDistance(MAX_LEVENSHTEIN_DISTANCE);
    }

    /**
     * Advanced string normalization for organization data
     */
    public static String normalizeForComparison(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String normalized = input.trim();

        // 1. Convert to lowercase
        normalized = normalized.toLowerCase();

        // 2. Remove diacritics (accents)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");

        // 3. Standardize common organization terms (Nigerian context)
        normalized = normalized
                // Company types
                .replaceAll("\\b(limited|ltd\\.?)\\b", "ltd")
                .replaceAll("\\b(incorporated|inc\\.?)\\b", "inc")
                .replaceAll("\\b(corporation|corp\\.?)\\b", "corp")
                .replaceAll("\\b(company|co\\.?)\\b", "co")
                .replaceAll("\\b(enterprises|ent\\.?)\\b", "ent")
                .replaceAll("\\b(ventures|vent\\.?)\\b", "vent")
                .replaceAll("\\b(services|svcs?\\.?)\\b", "svc")
                .replaceAll("\\b(associates|assoc\\.?)\\b", "assoc")

                // Nigerian-specific terms
                .replaceAll("\\b(nigeria|ng\\.?|nig\\.?)\\b", "nigeria")
                .replaceAll("\\b(limited by guarantee|lbg)\\b", "lbg")

                // Address abbreviations (Nigerian context)
                .replaceAll("\\b(street|str\\.?|st\\.?)\\b", "st")
                .replaceAll("\\b(avenue|ave\\.?)\\b", "ave")
                .replaceAll("\\b(road|rd\\.?)\\b", "rd")
                .replaceAll("\\b(boulevard|blvd\\.?)\\b", "blvd")
                .replaceAll("\\b(close|cl\\.?)\\b", "cl")
                .replaceAll("\\b(crescent|cres\\.?)\\b", "cres")
                .replaceAll("\\b(drive|dr\\.?)\\b", "dr")
                .replaceAll("\\b(lane|ln\\.?)\\b", "ln")
                .replaceAll("\\b(way|wy\\.?)\\b", "wy")
                .replaceAll("\\b(estate|est\\.?)\\b", "est")
                .replaceAll("\\b(village|vlg\\.?|vil\\.?)\\b", "vil")
                .replaceAll("\\b(local government|lg\\.?|lga)\\b", "lga")

                // Building terms
                .replaceAll("\\b(suite|ste\\.?)\\b", "ste")
                .replaceAll("\\b(floor|fl\\.?)\\b", "fl")
                .replaceAll("\\b(apartment|apt\\.?)\\b", "apt")
                .replaceAll("\\b(block|blk\\.?)\\b", "blk")
                .replaceAll("\\b(flat|flt\\.?)\\b", "flt");

        // 4. Remove common filler words (optional, can be aggressive)
        normalized = normalized
                .replaceAll("\\b(the|and|of|for|in|at|on|to)\\b", "")
                .replaceAll("\\s+", " "); // Clean up extra spaces

        // 5. Remove all non-alphanumeric characters except spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");

        // 6. Remove extra whitespace and trim
        normalized = normalized.trim().replaceAll("\\s+", " ");

        return normalized;
    }

    /**
     * Calculate similarity percentage using Levenshtein Distance
     */
    public double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }

        if (str1.isEmpty() && str2.isEmpty()) {
            return 100.0;
        }

        // Calculate Levenshtein distance
        Integer distance = levenshteinDistance.apply(str1, str2);

        if (distance == null) {
            return 0.0;
        }

        // Calculate similarity percentage
        int maxLength = Math.max(str1.length(), str2.length());

        if (maxLength == 0) {
            return 100.0;
        }

        double similarity = (1.0 - (double) distance / maxLength) * 100.0;
        return Math.max(0.0, Math.min(100.0, similarity)); // Clamp to 0-100
    }

    /**
     * Advanced organization name comparison with multiple strategies
     */
    public NameComparisonResult compareOrganizationNames(String claimedName, String officialName) {
        NameComparisonResult result = new NameComparisonResult();

        if (claimedName == null || officialName == null) {
            result.setMatch(false);
            result.setSimilarity(0.0);
            result.setReason("Missing name data");
            return result;
        }

        // Strategy 1: Direct normalized comparison
        String normalizedClaimed = normalizeForComparison(claimedName);
        String normalizedOfficial = normalizeForComparison(officialName);

        double similarity = calculateSimilarity(normalizedClaimed, normalizedOfficial);
        result.setSimilarity(similarity);

        // Strategy 2: Check for exact match after normalization
        if (normalizedClaimed.equals(normalizedOfficial)) {
            result.setMatch(true);
            result.setReason("Exact match after normalization");
            return result;
        }

        // Strategy 3: Check if one contains the other (for partial matches)
        boolean containsMatch = normalizedClaimed.contains(normalizedOfficial) ||
                normalizedOfficial.contains(normalizedClaimed);

        // Strategy 4: Extract and compare core name (remove company suffixes)
        String coreClaimed = extractCoreName(claimedName);
        String coreOfficial = extractCoreName(officialName);
        String normalizedCoreClaimed = normalizeForComparison(coreClaimed);
        String normalizedCoreOfficial = normalizeForComparison(coreOfficial);

        double coreSimilarity = calculateSimilarity(normalizedCoreClaimed, normalizedCoreOfficial);

        // Determine match using combined logic
        boolean isMatch = similarity >= NAME_SIMILARITY_THRESHOLD ||
                (containsMatch && similarity >= 70.0) ||
                (coreSimilarity >= 90.0);

        result.setMatch(isMatch);

        // Build detailed reason
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Overall similarity: %.1f%%", similarity));
        if (containsMatch) reason.append(", Contains match");
        if (coreSimilarity >= 90.0) reason.append(", Core name similarity: ").append(coreSimilarity).append("%");
        result.setReason(reason.toString());

        return result;
    }

    /**
     * Extract core organization name (remove common suffixes)
     */
    private String extractCoreName(String organizationName) {
        if (organizationName == null) return "";

        // Remove common suffixes and legal entities
        return organizationName
                .replaceAll("(?i)\\s+(limited|ltd\\.?|incorporated|inc\\.?|corporation|corp\\.?|company|co\\.?|plc|llc)$", "")
                .replaceAll("(?i)^(the)\\s+", "")
                .trim();
    }

    /**
     * Address comparison with component-based matching
     */
    public AddressComparisonResult compareAddresses(Address claimedAddress, Address officialAddress) {
        AddressComparisonResult result = new AddressComparisonResult();

        if (claimedAddress == null || officialAddress == null) {
            result.setMatch(false);
            result.setSimilarity(0.0);
            result.setReason("Missing address data");
            return result;
        }

        // Normalize addresses
        String normalizedAddressClaimed = normalizeForComparison(claimedAddress.address());
        String normalizedAddressOfficial = normalizeForComparison(officialAddress.address());

        // Calculate overall similarity
        double addressSimilarity = calculateSimilarity(normalizedAddressClaimed, normalizedAddressOfficial);

        String normalizedLgaClaimed = normalizeForComparison(claimedAddress.LGA());
        String normalizedLgaOfficial = normalizeForComparison(officialAddress.LGA());

        double lgaSimilarity = calculateSimilarity(normalizedLgaClaimed, normalizedLgaOfficial);

        String normalizedStateClaimed = normalizeForComparison(claimedAddress.state());
        String normalizedStateOfficial = normalizeForComparison(officialAddress.state());

        double stateSimilarity = calculateSimilarity(normalizedStateClaimed, normalizedStateOfficial);

        double similarity = (stateSimilarity * 0.6) + (lgaSimilarity * 0.3) + (addressSimilarity * 0.1);
        result.setSimilarity(similarity);

        // Split into components for granular comparison
        List<String> claimedComponents = splitAddressComponents(normalizedAddressClaimed);
        List<String> officialComponents = splitAddressComponents(normalizedAddressClaimed);

        // Check for key component matches (postal codes, building numbers)
        int matchingComponents = countMatchingComponents(claimedComponents, officialComponents);
        int totalComponents = Math.max(claimedComponents.size(), officialComponents.size());
        double componentMatchRatio = totalComponents > 0 ?
                (double) matchingComponents / totalComponents * 100 : 0.0;

        // Determine match - addresses can be more flexible
        boolean isMatch = similarity >= ADDRESS_SIMILARITY_THRESHOLD ||
                (componentMatchRatio >= 60.0 && similarity >= 50.0);

        result.setMatch(isMatch);
        result.setComponentMatchRatio(componentMatchRatio);

        // Build reason
        StringBuilder reason = new StringBuilder();
        reason.append(String.format("Similarity: %.1f%%", similarity));
        reason.append(String.format(", Component match: %.1f%%", componentMatchRatio));
        if (matchingComponents > 0) reason.append(" (").append(matchingComponents).append(" components match)");
        result.setReason(reason.toString());

        return result;
    }

    /**
     * Split address into meaningful components
     */
    private List<String> splitAddressComponents(String address) {
        if (address == null || address.isEmpty()) {
            return new ArrayList<>();
        }

        // Split by common delimiters and filter out empty/short components
        List<String> components = new ArrayList<>();
        String[] parts = address.split("[,\\s]+");

        for (String part : parts) {
            if (part.length() >= 2) { // Ignore very short components
                components.add(part);
            }
        }

        return components;
    }

    /**
     * Count matching address components
     */
    private int countMatchingComponents(List<String> components1, List<String> components2) {
        Set<String> set2 = new HashSet<>(components2);
        int matches = 0;

        for (String component : components1) {
            if (set2.contains(component)) {
                matches++;
            } else {
                // Check for partial matches (useful for numbers)
                for (String target : set2) {
                    if (component.matches("\\d+") && target.matches("\\d+")) {
                        // For numbers, check if they're close
                        try {
                            int num1 = Integer.parseInt(component);
                            int num2 = Integer.parseInt(target);
                            if (Math.abs(num1 - num2) <= 5) { // Numbers within 5 of each other
                                matches++;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Not a number, continue
                        }
                    }
                }
            }
        }

        return matches;
    }

    /**
     * Complete organization verification
     */
    public VerificationResult verifyOrganization(OrganizationClaim claim, CACRecord record) {
        VerificationResult result = new VerificationResult();

        // 1. CAC Registration Number (exact match required)
        boolean cacMatch = compareCacNumber(claim.getCacRegistrationNumber(),
                record.getRegistrationNumber());
        result.setCacNumberMatch(cacMatch);

        if (!cacMatch) {
            result.setOverallVerified(false);
            result.setVerificationStatus(ReviewStatus.Rejected);
            result.addMessage("CAC registration number does not match");
            return result;
        }

        // 2. Organization Name Comparison
        NameComparisonResult nameResult = compareOrganizationNames(
                claim.getOrganizationName(),
                record.getRegisteredName()
        );
        result.setNameMatch(nameResult.getMatch());
        result.setNameSimilarity(nameResult.getSimilarity());
        result.addMessage("Name: " + nameResult.getReason());

        // 3. Address Comparison
        AddressComparisonResult addressResult = compareAddresses(
                claim.getRegisteredAddress(),
                record.getRegisteredAddress()
        );
        result.setAddressMatch(addressResult.isMatch());
        result.setAddressSimilarity(addressResult.getSimilarity());
        result.addMessage("Address: " + addressResult.getReason());

        // 4. Determine overall status
        if (nameResult.getMatch() && addressResult.isMatch()) {
            result.setOverallVerified(true);
            result.setVerificationStatus(ReviewStatus.Approved);
        } else if (nameResult.getSimilarity() >= 70.0 && addressResult.isMatch()) {
            // Borderline case - flag for manual review
            result.setOverallVerified(false);
            result.addMessage("Flagged for manual review: Name similarity is borderline");
        } else {
            result.setOverallVerified(false);
            result.setVerificationStatus(ReviewStatus.Rejected);
        }

        // Calculate confidence score (0-100)
        double confidence = calculateConfidenceScore(result);
        result.setConfidenceScore(confidence);

        return result;
    }

    /**
     * Compare CAC numbers with cleaning
     */
    private boolean compareCacNumber(String claimed, String official) {
        if (claimed == null || official == null) {
            return false;
        }

        // Remove common formatting characters
        String cleanClaimed = claimed.replaceAll("[^0-9]", "").toUpperCase();
        String cleanOfficial = official.replaceAll("[^0-9]", "").toUpperCase();
        return cleanClaimed.equals(cleanOfficial);
    }

    /**
     * Calculate overall confidence score
     */
    private double calculateConfidenceScore(VerificationResult result) {
        double score = 0.0;

        if (result.isCacNumberMatch()) score += 40.0;
        score += result.getNameSimilarity() * 0.3; // 30% weight
        score += result.getAddressSimilarity() * 0.3; // 30% weight

        return Math.min(100.0, score);
    }


    @Setter
    @Getter
    public static class AddressComparisonResult {
        private boolean match;
        private double similarity;
        private double componentMatchRatio;
        private String reason;
        // Getters and setters
    }


    // Example test
    public static void main(String[] args) {
        OrganizationVerifier verifier = new OrganizationVerifier();


        // Test cases
        //"123 Awolowo Road, Ikoyi, Lagos"
        testCase(verifier,
                "rc12345",
                "Green Energy Solutions Ltd.",
                new Address("123 Awolowo Road", "Ikoyi", "Lagos"),
                "12345",
                "Green Energy Solutions Limited",
                new Address("123 Awolowo Rd", "Ikoyi", "Lagos")
        );

        testCase(verifier,
                "XYZ-789",
                "Tech Innovators Co.",
                new Address("Suite 5, Block B", "Victoria Island", "Lagos"),
                "XYZ-789",
                "Tech Innovators Company",
                new Address("", "Victoria island", "Lagos")
        );
    }

    private static void testCase(OrganizationVerifier verifier,
                                 String claimedCac, String claimedName, Address claimedAddr,
                                 String officialCac, String officialName, Address officialAddr) {

        OrganizationClaim claim = new OrganizationClaim(claimedCac, claimedName, claimedAddr);
        CACRecord record = new CACRecord(officialCac, officialName, officialAddr);

        VerificationResult result = verifier.verifyOrganization(claim, record);

        System.out.println("\n=== Verification Test ===");
        System.out.println("Claimed: " + claimedName);
        System.out.println("Official: " + officialName);
        System.out.println("Status: " + result.getVerificationStatus());
        System.out.println("Confidence: " + result.getConfidenceScore() + "%");
        System.out.println("Name Similarity: " + result.getNameSimilarity() + "%");
        System.out.println("Address Similarity: " + result.getAddressSimilarity() + "%");
        System.out.println("Messages: " + result.getMessages());
    }
}

