package com.example.catalog;

import java.util.List;
import java.util.Optional;

public interface CatalogReadClient {
  Optional<CatalogItem> getItem(Sku sku);

  BatchReadResponse batchGetItems(List<Sku> skus);
}
