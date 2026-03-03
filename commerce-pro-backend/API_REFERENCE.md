# Commerce Pro Backend - API Reference

## Base URL
```
http://localhost:8080/api
```

## Product API Endpoints

### CRUD Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | List all products (paginated) |
| GET | `/products/{id}` | Get product by ID |
| POST | `/products` | Create new product |
| PUT | `/products/{id}` | Update product (full) |
| PATCH | `/products/{id}` | Partial update product |
| DELETE | `/products/{id}` | Delete product |

### Search & Filter

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/search?query={query}` | Search products |
| GET | `/products/category/{category}` | Get by category |
| GET | `/products/featured` | Get featured products |

### Stock Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/products/{id}/stock` | Update stock |
| GET | `/products/stock/low` | Get low stock products |
| GET | `/products/stock/out-of-stock` | Get out of stock products |

### Dashboard & Statistics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/dashboard/top-selling` | Top selling products |
| GET | `/products/dashboard/top-revenue` | Top revenue products |
| GET | `/products/stats` | Product statistics |

### Reference Data

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products/reference/brands` | Get all brands |
| GET | `/products/reference/tags` | Get all tags |
| GET | `/products/validate/sku?sku={sku}` | Check SKU availability |

### Bulk Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/products/bulk-delete` | Delete multiple products |
| POST | `/products/bulk-status` | Update status for multiple |

## File Upload API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/files/upload/product` | Upload product image |
| POST | `/files/upload` | Upload generic file |
| GET | `/files/download/{fileName}` | Download file |
| GET | `/files/view/{fileName}` | View file (inline) |
| DELETE | `/files/{fileName}` | Delete file |

## Query Parameters

### Pagination (for list endpoints)
- `page` - Page number (0-based)
- `size` - Items per page (default: 20, max: 100)
- `sort` - Sort field and direction (e.g., `name,asc` or `price,desc`)

### Filter Parameters (for `/products`)
- `search` - Search in name, SKU, description, brand
- `status` - Filter by status: `active`, `draft`, `archived`, `out_of_stock`, `discontinued`
- `category` - Filter by category
- `stockStatus` - Filter by stock: `in_stock`, `low_stock`, `out_of_stock`
- `brand` - Filter by brand
- `minPrice` / `maxPrice` - Price range filter
- `minRating` - Minimum rating (0-5)
- `featured` - Filter featured products (`true`/`false`)
- `sortBy` - Sort field
- `sortDirection` - Sort direction (`asc` or `desc`)

## Response Format

All API responses follow a standard structure:

```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": 1709999999999,
  "path": "/api/products"
}
```

### Paginated Response

```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

## H2 Console

Access H2 database console at: `http://localhost:8080/api/h2-console`

**JDBC URL:** `jdbc:h2:file:./data/commerce-pro-db`
**Username:** `admin`
**Password:** `admin`

## Product Status Values

- `active` - Product is active and visible
- `draft` - Product is in draft state
- `archived` - Product is archived
- `out_of_stock` - Product is out of stock
- `discontinued` - Product is discontinued

## Stock Status Values

- `in_stock` - Stock is above low stock threshold
- `low_stock` - Stock is at or below threshold but > 0
- `out_of_stock` - Stock is 0 or less

## Visibility Values

- `visible` - Visible in storefront
- `hidden` - Hidden from storefront

## Example Requests

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "sku": "PROD-001",
    "category": "Electronics",
    "brand": "BrandName",
    "price": 99.99,
    "stock": 100,
    "lowStockThreshold": 10,
    "status": "active",
    "visibility": "visible",
    "featured": false,
    "trackInventory": true,
    "allowBackorders": false
  }'
```

### Get Products with Filter
```bash
curl "http://localhost:8080/api/products?status=active&category=Electronics&page=0&size=10&sort=price,desc"
```

### Update Stock
```bash
curl -X POST http://localhost:8080/api/products/{id}/stock \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 50,
    "reason": "Restock",
    "adjust": false
  }'
```

### Upload Image
```bash
curl -X POST http://localhost:8080/api/files/upload/product \
  -F "file=@/path/to/image.jpg"
```
