# Kartazze
A little Orm/QueryBuilder lib experiment with Kotlin.
Tried a few, [Exposed](https://github.com/JetBrains/Exposed), [ActiveJDBC](https://javalite.io/activejdbchttps://javalite.io/activejdbc), [Norm](https://github.com/dieselpoint/norm) and I hated them all.

I decided as an experiment to build one myself.

## TODO
- [x] Timestamps
- [x] OrderBy
- [x] @Table
- [x] Truncate
- [ ] Where in
- [ ] Multiple Sql Drivers


### Migration
- [ ] ReplaceTable

### Mapping
- [ ] Enum/JsonField Serializer
- [ ] Remove Maps
  - [ ] Rename fields with annotations

### Complex Operations
- [x] Joins
- [x] Relationships
- [ ] Deeply nested Associations/Fetches
- [ ] Random Aliasing for columns to avoid conflicts