package com.nviaud.pricing.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Version

@Entity
class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    var id: Long? = null

    @Column(unique = true, nullable = false)
    var name: String? = null

    @OneToMany(mappedBy = "category")
    var products: MutableList<Product>? = null

    @Version
    @JsonIgnore
    var version: Long? = null

}