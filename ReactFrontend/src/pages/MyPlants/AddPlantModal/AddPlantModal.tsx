import React from "react";
import { AddPlantModalStyled } from "./AddPlantModal.styled";
import { SpringValue } from "react-spring";
import { PlantEntryLiStyled } from "./PlantEntryLi.styled";
import { Plant } from "models/Plant";

type Props = {
  onOverlayClick: React.Dispatch<React.SetStateAction<boolean>>;
  style: {
    transform: SpringValue<string>;
  };
  allPlantList: Plant[];
};

export default function AddPlantModal({
  onOverlayClick,
  style,
  allPlantList,
}: Props) {
  /*categoryClickHandler
    click occurs
    1. Get plant list from parent
    2. Map through list and render
    3. Wait for click
    4. Add plant. Post request.
    5. Close modal.
  */

  // const PlantLiClickHandler = (
  //   e: React.MouseEvent<HTMLLIElement, MouseEvent>
  // ) => {
  //   const newActiveCategoryName: string = e.currentTarget.innerHTML;
  //   onOverlayClick(false);
  //   const newActiveCategory = categories.find((category) => {
  //     return category.name === newActiveCategoryName;
  //   });
  //   const activeOffsetY =
  //     newActiveCategory !== undefined ? newActiveCategory.offsetY : 0;

  //   window.scrollTo({ top: activeOffsetY - HEADER_HEIGHT, behavior: "smooth" });
  // };

  return (
    <AddPlantModalStyled style={style}>
      <h4>Add Plants</h4>
      <ul>
        {allPlantList.map((plantItem, index) => {
          return (
            <PlantEntryLiStyled key={index} onClick={() => {}}>
              {plantItem.sk}
            </PlantEntryLiStyled>
          );
        })}
      </ul>
    </AddPlantModalStyled>
  );
}
