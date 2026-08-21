package com.app.dating.matching;

import java.util.List;

public record MatchesResponse(List<MatchSummaryDto> matches) {
}
