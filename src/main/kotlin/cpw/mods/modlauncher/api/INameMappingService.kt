package cpw.mods.modlauncher.api

// This isn't actually to be used by anything related to Kilt,
// but this is implemented for the sake of mod compatibility
interface INameMappingService {
    enum class Domain {
        CLASS, METHOD, FIELD
    }
}