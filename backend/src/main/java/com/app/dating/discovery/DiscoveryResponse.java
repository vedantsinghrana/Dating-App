package com.app.dating.discovery;

import java.util.List;

public record DiscoveryResponse(List<DiscoveryResultDto> results, boolean hasMore) {
}
