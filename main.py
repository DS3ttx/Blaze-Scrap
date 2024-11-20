from selenium import webdriver
from datetime import datetime
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC


LINK = "https://blaze.com/pt/games/double?modal=profile&user_id="


class Bet:
    player_id: str
    round_id: int
    target_id: int
    amount: float

    def __init__(self, pid: str, rid: int, tig: int, amount: float):
        # Change this init for receive elements from selenium
        self.player_id = pid
        self.round_id = rid
        self.target_id = tig
        self.amount = amount

    def save(self, cursor):
        # Save in database
        pass


class Round:
    _id: int
    winner: int
    date: datetime

    amount_red: float
    amount_black: float
    amount_white: float

    total_players_red: int
    total_players_black: int
    total_players_white: int

    def __init__(self):
        # Make this init for receive elements from selenium
        pass

    def make_winner(self, winner_id: int):
        self.winner = winner_id

    def save(self):
        # Save in database
        pass


if __name__ == '__main__':
    browser = webdriver.Firefox()
    wait = WebDriverWait(browser, 10)
    browser.get("https://blaze.com/pt/games/double")

    entriesBet = set()
    processed = False

    while True:
        roulette = wait.until(EC.presence_of_element_located((By.ID, 'roulette')))
        state = roulette.get_attribute("class")

        if state == "page waiting":
            processed = False
            columns = wait.until(EC.presence_of_all_elements_located((By.CLASS_NAME, 'roulette-column')))

            for column in columns:
                for entry in column.find_elements(By.CLASS_NAME, "entry"):
                    try:
                        username = entry.find_element(By.CLASS_NAME, 'user-profile-link').get_attribute('href')
                        username = username.replace(LINK, "")
                        amount = entry.find_element(By.CLASS_NAME, "amount ").text
                        entriesBet.add((username, amount))
                    except Exception:
                        pass

        elif state == "page rolling" and not processed:
            processed = True
            for bet in entriesBet:
                print(bet[0], bet[1])

            entriesBet.clear()
            print("\n\n")
