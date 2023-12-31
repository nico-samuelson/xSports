package proyek.andro.model

class Tournament : BaseModel {
    lateinit var id : String
    lateinit var game : String
    lateinit var name : String
    lateinit var start_date : String
    lateinit var end_date : String
    var prize_pool : Long = 0
    lateinit var organizer : String
    lateinit var type : String
    lateinit var location : List<String>
    lateinit var venue : List<String>
    lateinit var description : String
    lateinit var logo : String
    lateinit var banner : String
    var status : Long = 0

    constructor() : super("tbTournament") {}

    constructor(
        id: String,
        game: String,
        name: String,
        start_date: String,
        end_date: String,
        prize_pool: Long,
        organizer: String,
        type: String,
        location: List<String>,
        venue: List<String>,
        description: String,
        logo: String,
        banner: String,
        status: Long
    ) : super("tbTournament") {
        this.id = id
        this.game = game
        this.name = name
        this.start_date = start_date
        this.end_date = end_date
        this.prize_pool = prize_pool
        this.organizer = organizer
        this.type = type
        this.location = location
        this.venue = venue
        this.description = description
        this.logo = logo
        this.banner = banner
        this.status = status
    }

    fun convertToMap() : Map<String, Any> {
        val data = HashMap<String, Any>()

        data.put("id", id)
        data.put("game", game)
        data.put("name", name)
        data.put("start_date", start_date)
        data.put("end_date", end_date)
        data.put("prize_pool", prize_pool)
        data.put("organizer", organizer)
        data.put("type", type)
        data.put("location", location)
        data.put("venue", venue)
        data.put("description", description)
        data.put("logo", logo)
        data.put("banner", banner)
        data.put("status", status)

        return data
    }
}