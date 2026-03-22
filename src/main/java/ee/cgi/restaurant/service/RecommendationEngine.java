package ee.cgi.restaurant.service;

import ee.cgi.restaurant.model.Preference;
import ee.cgi.restaurant.model.RecommendationOption;
import ee.cgi.restaurant.model.RestaurantTable;
import ee.cgi.restaurant.model.SearchCriteria;
import ee.cgi.restaurant.model.TableFeature;
import ee.cgi.restaurant.model.Zone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RecommendationEngine {

    public List<RecommendationOption> recommend(List<RestaurantTable> tables, Set<String> occupiedTableIds, SearchCriteria criteria) {
        List<Candidate> candidates = new ArrayList<>();
        for (RestaurantTable table : tables) {
            if (occupiedTableIds.contains(table.id())) {
                continue;
            }
            if (criteria.zone() != null && table.zone() != criteria.zone()) {
                continue;
            }
            candidates.add(scoreSingle(table, criteria));
            for (String neighborId : table.adjacentTableIds()) {
                tables.stream()
                        .filter(other -> other.id().equals(neighborId))
                        .findFirst()
                        .ifPresent(other -> {
                            if (occupiedTableIds.contains(other.id()) || other.id().equals(table.id())) {
                                return;
                            }
                            if (criteria.zone() != null && other.zone() != criteria.zone()) {
                                return;
                            }
                            if (table.id().compareTo(other.id()) >= 0) {
                                return;
                            }
                            candidates.add(scoreCombined(table, other, criteria));
                        });
            }
        }

        return candidates.stream()
                .sorted(Comparator.comparingInt(Candidate::score).reversed()
                        .thenComparingInt(Candidate::seats)
                        .thenComparing(candidate -> candidate.combined() ? 1 : 0))
                .limit(5)
                .map(candidate -> new RecommendationOption(
                        candidate.tableIds(),
                        candidate.seats(),
                        candidate.score(),
                        candidate.reason(),
                        candidate.combined()
                ))
                .toList();
    }

    private Candidate scoreSingle(RestaurantTable table, SearchCriteria criteria) {
        int score = baseFitScore(table.seats(), criteria.partySize());
        score += preferenceScore(table, criteria.preferences(), criteria.zone());
        score += Math.max(0, 20 - Math.abs(table.seats() - criteria.partySize()) * 4);
        String reason = buildReason(table, criteria, false);
        return new Candidate(List.of(table.id()), table.seats(), score, false, reason);
    }

    private Candidate scoreCombined(RestaurantTable left, RestaurantTable right, SearchCriteria criteria) {
        int seats = left.seats() + right.seats();
        int score = baseFitScore(seats, criteria.partySize());
        score += preferenceScore(left, criteria.preferences(), criteria.zone());
        score += preferenceScore(right, criteria.preferences(), criteria.zone());
        score += 12;
        score -= 8; // slight penalty for using multiple tables
        String reason = buildReason(left, criteria, true) + " + " + right.label();
        return new Candidate(List.of(left.id(), right.id()), seats, score, true, reason);
    }

    private int baseFitScore(int seats, int partySize) {
        int surplus = Math.max(0, seats - partySize);
        int shortage = Math.max(0, partySize - seats);
        return 120 - surplus * 18 - shortage * 30;
    }

    private int preferenceScore(RestaurantTable table, EnumSet<Preference> preferences, Zone zonePreference) {
        if (preferences == null || preferences.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (Preference preference : preferences) {
            score += switch (preference) {
                case PRIVACY -> has(table, TableFeature.QUIET) || table.zone() == Zone.PRIVATE_ROOM ? 16 : 0;
                case WINDOW -> has(table, TableFeature.WINDOW) || has(table, TableFeature.TERRACE_VIEW) ? 14 : 0;
                case ACCESSIBLE -> has(table, TableFeature.ACCESSIBLE) ? 14 : 0;
                case NEAR_PLAY_AREA -> has(table, TableFeature.NEAR_PLAY_AREA) ? 14 : 0;
            };
        }
        if (zonePreference != null && table.zone() == zonePreference) {
            score += 8;
        }
        return score;
    }

    private boolean has(RestaurantTable table, TableFeature feature) {
        return table.features().contains(feature);
    }

    private String buildReason(RestaurantTable table, SearchCriteria criteria, boolean combined) {
        List<String> reasons = new ArrayList<>();
        int surplus = table.seats() - criteria.partySize();
        if (surplus >= 0) {
            reasons.add(surplus == 0 ? "Exact fit" : "Only " + surplus + " spare seat(s)");
        } else {
            reasons.add("Needs " + (-surplus) + " more seat(s)");
        }
        if (criteria.preferences() != null) {
            for (Preference pref : criteria.preferences()) {
                if (pref == Preference.PRIVACY && (has(table, TableFeature.QUIET) || table.zone() == Zone.PRIVATE_ROOM)) {
                    reasons.add("privacy match");
                }
                if (pref == Preference.WINDOW && (has(table, TableFeature.WINDOW) || has(table, TableFeature.TERRACE_VIEW))) {
                    reasons.add("window match");
                }
                if (pref == Preference.ACCESSIBLE && has(table, TableFeature.ACCESSIBLE)) {
                    reasons.add("accessible");
                }
                if (pref == Preference.NEAR_PLAY_AREA && has(table, TableFeature.NEAR_PLAY_AREA)) {
                    reasons.add("near kids area");
                }
            }
        }
        if (combined) {
            reasons.add("combined tables");
        }
        return String.join(", ", reasons);
    }

    private record Candidate(List<String> tableIds, int seats, int score, boolean combined, String reason) {
        private Candidate {
            tableIds = List.copyOf(tableIds);
        }
    }
}
