package com.immomio.tidal.music.dto;

import java.util.List;

public record TidalAlbumRelationshipResponse(List<TidalAlbumData> data, List<TidalAlbumData> included) {
}
