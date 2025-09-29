package com.nviaud.pricing.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["name", "brand", "specifications"]
        )
    ]
)
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    var id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var brand: String? = null

    @ManyToOne(optional = false)
    var category: Category? = null

    /**
     * Store semi structured product specifications as JSON.
     * Example: {"color": "red", "material": "plastic"}
     * An alternative would be to use a document database like MongoDB.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    var specifications: Specifications? = null

    @Version
    @JsonIgnore
    var version: Long? = null

}