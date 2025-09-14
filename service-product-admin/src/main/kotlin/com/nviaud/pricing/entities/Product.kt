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

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = ["name", "brand", "height", "width", "depth", "weight"]
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
    var category: ProductCategory? = null

    var height: Int? = null
    var width: Int? = null
    var depth: Int? = null
    var weight: Int? = null

    @Version
    @JsonIgnore
    var version: Long? = null

}