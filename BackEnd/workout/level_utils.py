REQUIRED_EXP_BY_LEVEL = [
    50, 70, 90, 120, 150, 180, 220, 260, 300, 350,
    400, 450, 500, 550, 600, 650, 700, 750, 800, 900,
    1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 2000,
    2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000,
    4300, 4600, 4900, 5200, 5500, 5800, 6100, 6400, 6700, 7000,
    7400, 7800, 8200, 8600, 9000, 9400, 9800, 10200, 10600, 11000,
    11500, 12000, 12500, 13000, 13500, 14000, 14500, 15000, 15500, 16000,
    16600, 17200, 17800, 18400, 19000, 19600, 20200, 20800, 21400, 22000,
    22700, 23400, 24100, 24800, 25500, 26200, 26900, 27600, 28300, 29000,
    29800, 30600, 31400, 32200, 33000, 33800, 34600, 35400, 36200,
]


def get_required_exp(level):
    if level < 1 or level >= 100:
        return 0

    return REQUIRED_EXP_BY_LEVEL[level - 1]


def calculate_level_info(total_exp):
    total_exp = int(total_exp or 0)

    level = 1
    cumulative_before_current_level = 0

    for required_exp in REQUIRED_EXP_BY_LEVEL:
        if level >= 100:
            break

        if total_exp >= cumulative_before_current_level + required_exp:
            cumulative_before_current_level += required_exp
            level += 1
        else:
            break

    exp_required = get_required_exp(level)

    if level >= 100:
        level_exp = 0
        exp_required = 0
    else:
        level_exp = total_exp - cumulative_before_current_level

    return {
        "level": level,
        "exp": total_exp,
        "level_exp": level_exp,
        "exp_required": exp_required,
    }


def add_exp_and_level_up(user, gained_exp):
    user.exp += int(gained_exp or 0)
    user.save(update_fields=["exp"])
    return user
