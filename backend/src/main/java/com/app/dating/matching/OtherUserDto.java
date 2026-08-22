package com.app.dating.matching;

import java.util.List;

public record OtherUserDto(String userId, String name, List<String> photos) {
}
